package com.zetsuengine.catalyst;

import com.zetsuengine.common.ProjectPaths;
import com.zetsuengine.common.SteamCmdLauncher;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WorkspaceSetup {

    private static final String CFR_URL = "https://github.com/leibnitz27/cfr/releases/download/0.152/cfr-0.152.jar";

    public static void main(String[] args) throws Exception {
        ProjectPaths.ENVIRONMENT_PATH.mkdirs();

        if (!ProjectPaths.PZ_JAR_PATH.exists()) {
            SteamCmdLauncher.downloadAndRun(ProjectPaths.PZ_SERVER_PATH, ProjectPaths.STEAMCMD_PATH);
        }

        if (!ProjectPaths.PZ_JAR_PATH.exists()) {
            System.exit(1);
        }

        File cfrJar = new File(ProjectPaths.ENVIRONMENT_PATH, "cfr.jar");
        if (!cfrJar.exists()) {
            try (InputStream in = URI.create(CFR_URL).toURL().openStream()) {
                Files.copy(in, cfrJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        ProcessBuilder decompilePb = new ProcessBuilder(
                "java", "-jar", cfrJar.getAbsolutePath(),
                ProjectPaths.PZ_JAR_PATH.getAbsolutePath(),
                "--outputdir", ProjectPaths.WORKSPACE_PATH.getAbsolutePath()
        );
        decompilePb.inheritIO().start().waitFor();

        ProcessBuilder gitInitPb = new ProcessBuilder("git", "init");
        gitInitPb.directory(ProjectPaths.WORKSPACE_PATH);
        gitInitPb.inheritIO().start().waitFor();

        ProcessBuilder gitAddPb = new ProcessBuilder("git", "add", ".");
        gitAddPb.directory(ProjectPaths.WORKSPACE_PATH);
        gitAddPb.inheritIO().start().waitFor();

        ProcessBuilder gitCommitPb = new ProcessBuilder("git", "commit", "-m", "Initial commit");
        gitCommitPb.directory(ProjectPaths.WORKSPACE_PATH);
        gitCommitPb.inheritIO().start().waitFor();

        File patchesDir = ProjectPaths.PATCHES_PATH;
        if (patchesDir.exists() && patchesDir.listFiles() != null && patchesDir.listFiles().length > 0) {
            ProcessBuilder gitApplyPb = new ProcessBuilder("git", "am", "../zetsu-patches/patches/*.patch");
            gitApplyPb.directory(ProjectPaths.WORKSPACE_PATH);
            gitApplyPb.inheritIO().start().waitFor();
        }
    }
}

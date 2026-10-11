package com.zetsuengine.catalyst;

import com.zetsuengine.common.ProjectPaths;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class PatchExporter {

    public static void main(String[] args) throws Exception {
        if (ProjectPaths.PATCHES_PATH.exists()) {
            File[] files = ProjectPaths.PATCHES_PATH.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".patch")) {
                        file.delete();
                    }
                }
            }
        } else {
            ProjectPaths.PATCHES_PATH.mkdirs();
        }

        ProcessBuilder getRootPb = new ProcessBuilder("git", "rev-list", "--max-parents=0", "HEAD");
        getRootPb.directory(ProjectPaths.WORKSPACE_PATH);
        Process rootProcess = getRootPb.start();

        String rootCommit;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(rootProcess.getInputStream()))) {
            rootCommit = reader.readLine();
        }
        rootProcess.waitFor();

        // generate .patch files based on workspace commit history
        if (rootCommit != null && !rootCommit.isEmpty()) {
            ProcessBuilder exportPb = new ProcessBuilder(
                    "git", "format-patch",
                    "--zero-commit",
                    "--full-index",
                    "--no-signature",
                    "-N",
                    "-o", ProjectPaths.PATCHES_PATH.getAbsolutePath(),
                    rootCommit
            );
            exportPb.directory(ProjectPaths.WORKSPACE_PATH);
            exportPb.inheritIO().start().waitFor();
        }
    }
}

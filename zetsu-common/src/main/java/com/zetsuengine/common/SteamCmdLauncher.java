package com.zetsuengine.common;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SteamCmdLauncher {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private static final String CDN_URL = "https://steamcdn-a.akamaihd.net/client/installer";

    // WINDOWS
    private static final String ZIP_ARCHIVE_NAME = "steamcmd.zip";
    private static final String WIN_EXEC_NAME = "steamcmd.exe";

    // LINUX
    private static final String TAR_ARCHIVE_NAME = "steamcmd_linux.tar.gz";
    private static final String SHELL_EXEC_NAME = "steamcmd.sh";

    public static void downloadAndRun(File serverDir, File steamCmdDir) throws Exception {
        steamCmdDir.mkdirs();

        String archiveName = IS_WINDOWS ? ZIP_ARCHIVE_NAME : TAR_ARCHIVE_NAME;
        String execName = IS_WINDOWS ? WIN_EXEC_NAME : SHELL_EXEC_NAME;
        File archiveFile = new File(steamCmdDir, archiveName);

        downloadFile(archiveName, archiveFile);
        extractArchive(archiveFile, steamCmdDir);

        runSteamCmd(steamCmdDir, execName, "+quit");

        runSteamCmd(steamCmdDir, execName,
                "+force_install_dir", serverDir.getAbsolutePath(),
                "+login", "anonymous",
                "+app_info_update", "1",
                "+app_update", "380870", "validate",
                "+quit"
        );
    }

    private static void downloadFile(String fileName, File targetFile) throws Exception {
        try (InputStream in = URI.create(CDN_URL + "/" + fileName).toURL().openStream()) {
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void extractArchive(File archiveFile, File targetDir) throws Exception {
        if (IS_WINDOWS) {
            unzip(archiveFile, targetDir);
        } else {
            new ProcessBuilder("tar", "-xf", archiveFile.getAbsolutePath(), "-C", targetDir.getAbsolutePath())
                    .inheritIO().start().waitFor();
        }
    }

    private static void runSteamCmd(File steamCmdDir, String execName, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(new File(steamCmdDir, execName).getAbsolutePath());
        Collections.addAll(command, args);

        new ProcessBuilder(command)
                .directory(steamCmdDir)
                .inheritIO()
                .start()
                .waitFor();
    }

    private static void unzip(File zipFile, File destDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    Files.copy(zis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}

package com.zetsuengine.clip;

import com.zetsuengine.common.SteamCmdLauncher;
import io.sigpipe.jbsdiff.Patch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ZetsuClipper {

    public static void main(String[] args) throws Exception {
        File serverDir = new File("pz_server");
        File steamCmdDir = new File("steamcmd");
        File originalJar = new File(serverDir, "java" + File.separator + "projectzomboid.jar");
        File patchedJar = new File("zetsu_server.jar");

        if (!originalJar.exists()) {
            SteamCmdLauncher.downloadAndRun(serverDir, steamCmdDir);
        }

        File patchFile = File.createTempFile("zetsu", ".patch");
        try (InputStream is = ZetsuClipper.class.getResourceAsStream("/server.bpatch")) {
            if (is != null) {
                Files.copy(is, patchFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                System.exit(1);
            }
        }

        byte[] oldBytes = Files.readAllBytes(originalJar.toPath());
        byte[] patchBytes = Files.readAllBytes(patchFile.toPath());

        // patch original engine file with zetsu's binary patch
        try (FileOutputStream fos = new FileOutputStream(patchedJar)) {
            Patch.patch(oldBytes, patchBytes, fos);
        }
        patchFile.delete();

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", patchedJar.getAbsolutePath());
        pb.inheritIO();
        pb.start().waitFor();
    }
}

package com.zetsuengine.patch;

import io.sigpipe.jbsdiff.Diff;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class BinaryPatchBuilder {

    public static void main(String[] args) throws Exception {
        File originalJar = new File(args[0]);
        File modifiedJar = new File(args[1]);
        File outputPatch = new File(args[2]);

        byte[] oldBytes = Files.readAllBytes(originalJar.toPath());
        byte[] newBytes = Files.readAllBytes(modifiedJar.toPath());

        try (FileOutputStream fos = new FileOutputStream(outputPatch)) {
            Diff.diff(oldBytes, newBytes, fos);
        }
    }
}

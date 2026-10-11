package com.zetsuengine.catalyst;

import com.zetsuengine.common.ProjectPaths;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * The main purpose of this class is to quickly test workspace changes in a real runtime environment.
 * When you run the main method, the program copies all modified files from git status or git diff.
 * Then, it compiles these files and replaces the classes inside the target jar.
 * For legal reasons, you should not use this tool to create jars for public distribution.
 * The official way to distribute Zetsu is by applying patches to an existing engine jar.
 */
public class CatalystRunner {

    public static void main(String[] args) throws Exception {
        if (!ProjectPaths.PZ_JAR_PATH.exists()) {
            System.exit(1);
        }

        if (ProjectPaths.OUT_PATH.exists()) {
            deleteDirectory(ProjectPaths.OUT_PATH);
        }
        ProjectPaths.OUT_PATH.mkdirs();

        List<String> modifiedFiles = getModifiedJavaFiles();

        if (!modifiedFiles.isEmpty()) {
            List<String> javacArgs = new ArrayList<>();
            javacArgs.add("javac");
            javacArgs.add("-cp");

            File zetsuServerClasses = new File("zetsu-server/target/classes");
            File zetsuApiClasses = new File("zetsu-api/target/classes");

            String combinedClasspath = ProjectPaths.PZ_JAR_PATH.getAbsolutePath()
                    + File.pathSeparator + zetsuServerClasses.getAbsolutePath()
                    + File.pathSeparator + zetsuApiClasses.getAbsolutePath();

            javacArgs.add(combinedClasspath);

            javacArgs.add("-d");
            javacArgs.add(ProjectPaths.OUT_PATH.getAbsolutePath());

            for (String file : modifiedFiles) {
                javacArgs.add(new File(ProjectPaths.WORKSPACE_PATH, file).getAbsolutePath());
            }

            ProcessBuilder javacPb = new ProcessBuilder(javacArgs);
            javacPb.inheritIO();
            Process javacProcess = javacPb.start();
            int javacExit = javacProcess.waitFor();

            if (javacExit != 0) {
                System.exit(1);
            }
        }

        Files.copy(ProjectPaths.PZ_JAR_PATH.toPath(), ProjectPaths.PZ_TEST_JAR_PATH.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // updates the test engine jar with the latest compiled workspace changes
        if (ProjectPaths.OUT_PATH.exists() && ProjectPaths.OUT_PATH.listFiles() != null && Objects.requireNonNull(ProjectPaths.OUT_PATH.listFiles()).length > 0) {
            Map<String, String> env = new HashMap<>();
            URI uri = URI.create("jar:" + ProjectPaths.PZ_TEST_JAR_PATH.toURI());

            try (FileSystem zipFs = FileSystems.newFileSystem(uri, env)) {
                try (Stream<Path> paths = Files.walk(ProjectPaths.OUT_PATH.toPath())) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        try {
                            Path relative = ProjectPaths.OUT_PATH.toPath().relativize(path);
                            Path pathInZipfile = zipFs.getPath(relative.toString().replace("\\", "/"));
                            if (pathInZipfile.getParent() != null) {
                                Files.createDirectories(pathInZipfile.getParent());
                            }
                            Files.copy(path, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }

        List<String> runArgs = new ArrayList<>();
        runArgs.add("java");

        runArgs.add("-Djava.awt.headless=true");
        runArgs.add("-Dzomboid.steam=0");
        runArgs.add("-Dzomboid.znetlog=1");

        runArgs.add("-XX:+UseZGC");
        runArgs.add("-XX:-CreateCoredumpOnCrash");
        runArgs.add("-XX:-OmitStackTraceInFastThrow");
        runArgs.add("-Xms8g");
        runArgs.add("-Xmx8g");

        runArgs.add("-Djava.library.path=" + new File(ProjectPaths.PZ_SERVER_PATH, "natives").getAbsolutePath());

        File projectRoot = ProjectPaths.WORKSPACE_PATH.getParentFile();
        File zetsuServerClasses = new File(projectRoot, "zetsu-server/target/classes");
        File zetsuApiClasses = new File(projectRoot, "zetsu-api/target/classes");

        StringBuilder classpathBuilder = new StringBuilder();

        // include server api and impl in classpath
        classpathBuilder.append(ProjectPaths.OUT_PATH.getAbsolutePath())
                .append(File.pathSeparator).append(ProjectPaths.PZ_JAR_PATH.getAbsolutePath())
                .append(File.pathSeparator).append(zetsuServerClasses.getAbsolutePath())
                .append(File.pathSeparator).append(zetsuApiClasses.getAbsolutePath());

        // include libs in classpath so that the logger can work for example
        File libDir = new File(projectRoot, "zetsu-server/target/lib");
        if (libDir.exists() && libDir.isDirectory()) {
            File[] jars = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars != null) {
                for (File jar : jars) {
                    classpathBuilder.append(File.pathSeparator).append(jar.getAbsolutePath());
                }
            }
        }

        runArgs.add("-cp");
        runArgs.add(classpathBuilder.toString());

        runArgs.add("zombie.network.GameServer");
        runArgs.add("-statistic");
        runArgs.add("0");

        ProcessBuilder runPb = new ProcessBuilder(runArgs);
        runPb.directory(ProjectPaths.PZ_SERVER_PATH);
        runPb.inheritIO();

        Process serverProcess = runPb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (serverProcess.isAlive()) {
                serverProcess.destroy();
            }
        }));
        serverProcess.waitFor();
    }

    private static List<String> getModifiedJavaFiles() throws Exception {
        Set<String> files = new HashSet<>();

        ProcessBuilder pbStatus = new ProcessBuilder("git", "status", "--porcelain");
        pbStatus.directory(ProjectPaths.WORKSPACE_PATH);
        Process processStatus = pbStatus.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processStatus.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".java")) {
                    files.add(line.substring(3).trim());
                }
            }
        }
        processStatus.waitFor();

        ProcessBuilder pbDiff = new ProcessBuilder("git", "diff-tree", "--no-commit-id", "--name-only", "-r", "HEAD");
        pbDiff.directory(ProjectPaths.WORKSPACE_PATH);
        Process processDiff = pbDiff.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processDiff.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".java")) {
                    files.add(line.trim());
                }
            }
        }
        processDiff.waitFor();

        return new ArrayList<>(files);
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}

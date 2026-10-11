package com.zetsuengine.common;

import java.io.File;

public class ProjectPaths {

    public static final File WORKSPACE_PATH = new File("zetsu-workspace");
    public static final File PATCHES_PATH = new File("zetsu-patch/patches");
    public static final File OUT_PATH = new File("zetsu-out");
    public static final File ENVIRONMENT_PATH = new File(".zetsu-env");
    public static final File PZ_SERVER_PATH = new File(ProjectPaths.ENVIRONMENT_PATH, "pz_server");
    public static final File PZ_JAR_PATH = new File(ProjectPaths.PZ_SERVER_PATH, "java" + File.separator + "projectzomboid.jar");
    public static final File PZ_TEST_JAR_PATH = new File(ProjectPaths.PZ_SERVER_PATH, "java" + File.separator + "projectzomboid_test.jar");

}

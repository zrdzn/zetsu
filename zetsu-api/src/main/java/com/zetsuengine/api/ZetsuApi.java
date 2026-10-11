package com.zetsuengine.api;

public class ZetsuApi {

    private static Server serverInstance;

    public static Server getServer() {
        if (serverInstance == null) {
            throw new IllegalStateException("Zetsu API is not initialized yet.");
        }
        return serverInstance;
    }

    public static void setServer(Server server) {
        if (serverInstance != null) {
            throw new IllegalStateException("Zetsu API is already initialized.");
        }
        serverInstance = server;
    }
}

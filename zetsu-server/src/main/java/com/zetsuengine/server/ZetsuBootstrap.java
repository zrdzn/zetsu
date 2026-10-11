package com.zetsuengine.server;

import com.zetsuengine.api.ZetsuApi;

public class ZetsuBootstrap {

    private static ZetsuServer internalServer;

    public static void handleEngineStart() {
        internalServer = new ZetsuServer();
        ZetsuApi.setServer(internalServer);
        internalServer.serverStarted();
    }

    public static void handleEngineStop() {
        if (internalServer != null) {
            internalServer.serverStopped();
        }
    }
}

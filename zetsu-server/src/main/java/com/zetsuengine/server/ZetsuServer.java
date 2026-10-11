package com.zetsuengine.server;

import com.zetsuengine.api.Player;
import com.zetsuengine.api.Server;
import com.zetsuengine.api.event.EventBus;
import com.zetsuengine.api.plugin.PluginManager;
import com.zetsuengine.server.event.EventBusImpl;
import com.zetsuengine.server.plugin.PluginManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zombie.network.GameServer;

import java.util.List;

public class ZetsuServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZetsuServer.class);

    private final EventBus eventBus = new EventBusImpl();
    private final PluginManager pluginManager = new PluginManagerImpl();

    public void serverStarted() {
        LOGGER.info("Engine has successfully started.");

        LOGGER.info("Loading plugins...");
        pluginManager.loadAll();

        LOGGER.info("Enabling plugins...");
        pluginManager.enableAll();
    }

    public void serverStopped() {
        LOGGER.info("Disabling plugins...");
        pluginManager.disableAll();

        LOGGER.info("Engine has successfully stopped.");
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public List<Player> getOnlinePlayers() {
        return GameServer.Players.stream()
                .map(player -> (Player) new ZetsuPlayer(player))
                .toList();
    }
}
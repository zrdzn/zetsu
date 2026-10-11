package com.plugin.example;

import com.zetsuengine.api.ZetsuApi;
import com.zetsuengine.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExamplePlugin implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplePlugin.class);

    private ScheduledExecutorService scheduler;

    @Override
    public void loaded() {
        LOGGER.info("Plugin has been loaded!");
    }

    @Override
    public void enabled() {
        LOGGER.info("Plugin has been enabled!");

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            int playerCount = ZetsuApi.getServer().getOnlinePlayers().size();
            LOGGER.info("There are currently {} players on the server.", playerCount);
        }, 5, 10, TimeUnit.SECONDS);
    }

    @Override
    public void disabled() {
        LOGGER.info("Plugin has been disabled!");

        if (scheduler != null) {
            scheduler.shutdownNow();
            this.scheduler = null;
        }
    }
}

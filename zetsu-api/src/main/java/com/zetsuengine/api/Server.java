package com.zetsuengine.api;

import com.zetsuengine.api.event.EventBus;
import com.zetsuengine.api.plugin.PluginManager;

import java.util.List;

public interface Server {

    EventBus getEventBus();

    PluginManager getPluginManager();

    List<Player> getOnlinePlayers();
}
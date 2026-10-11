package com.zetsuengine.api;

import com.zetsuengine.api.event.EventBus;

import java.util.List;

public interface Server {

    EventBus getEventBus();

    List<Player> getOnlinePlayers();
}
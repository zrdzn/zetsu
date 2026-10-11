package com.zetsuengine.api.event;

public interface EventBus {
    void register(EventSubscriber subscriber);
    void unregister(EventSubscriber subscriber);
    <T extends Event> T dispatch(T event);
}
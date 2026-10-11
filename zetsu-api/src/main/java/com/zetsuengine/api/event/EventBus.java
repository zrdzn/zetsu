package com.zetsuengine.api.event;

import java.util.function.Consumer;

public interface EventBus {
    <T extends Event> void register(Class<T> eventClass, Consumer<T> handler);
    <T extends Event> void unregister(Class<T> eventClass, Consumer<T> handler);
    <T extends Event> T dispatch(T event);
}
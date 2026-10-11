package com.zetsuengine.server.event;

import com.zetsuengine.api.event.Event;
import com.zetsuengine.api.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBusImpl implements EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusImpl.class);

    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> handlers = new HashMap<>();

    @Override
    public <T extends Event> void register(Class<T> eventClass, Consumer<T> handler) {
        handlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public <T extends Event> void unregister(Class<T> eventClass, Consumer<T> handler) {
        List<Consumer<? extends Event>> eventHandlers = handlers.get(eventClass);
        if (eventHandlers != null) {
            eventHandlers.remove(handler);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> T dispatch(T event) {
        List<Consumer<? extends Event>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (Consumer<? extends Event> handler : eventHandlers) {
                try {
                    ((Consumer<T>) handler).accept(event);
                } catch (Exception ex) {
                    LOGGER.error("Could not dispatch event.", ex);
                }
            }
        }
        return event;
    }
}
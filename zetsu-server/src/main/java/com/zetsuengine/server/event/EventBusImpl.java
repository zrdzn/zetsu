package com.zetsuengine.server.event;

import com.zetsuengine.api.event.Event;
import com.zetsuengine.api.event.EventBus;
import com.zetsuengine.api.event.EventSubscriber;
import com.zetsuengine.api.event.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

public class EventBusImpl implements EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusImpl.class);

    private final Map<Class<? extends Event>, Map<EventSubscriber, List<Method>>> subscriptions = new HashMap<>();

    @Override
    public void register(EventSubscriber subscriber) {
        Arrays.stream(subscriber.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Subscribe.class))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                .forEach(method -> {
                    Class<? extends Event> eventType = method.getParameterTypes()[0].asSubclass(Event.class);

                    subscriptions.computeIfAbsent(eventType, _ -> new HashMap<>())
                            .computeIfAbsent(subscriber, _ -> new ArrayList<>())
                            .add(method);
                });
    }

    @Override
    public void unregister(EventSubscriber subscriber) {
        subscriptions.values().forEach(map -> map.remove(subscriber));
    }

    @Override
    public <T extends Event> T dispatch(T event) {
        Map<EventSubscriber, List<Method>> eventSubscribers = subscriptions.get(event.getClass());
        if (eventSubscribers != null) {
            for (Map.Entry<EventSubscriber, List<Method>> entry : eventSubscribers.entrySet()) {
                for (Method method : entry.getValue()) {
                    try {
                        method.invoke(entry.getKey(), event);
                    } catch (Exception ex) {
                        LOGGER.error("Cannot dispatch event.", ex);
                    }
                }
            }
        }
        return event;
    }
}
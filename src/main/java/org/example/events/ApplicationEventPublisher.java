package org.example.events;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ApplicationEventPublisher {
    public List<Listener> listeners = new ArrayList<>();

    protected void publishEvent(ApplicationEvent event) throws InvocationTargetException, IllegalAccessException {
        for (Listener listener : listeners) {
            if (listener.eventType.equals(event.getClass())) {
                listener.method.invoke(listener.eventListener, event);
            }
        }
    }
}

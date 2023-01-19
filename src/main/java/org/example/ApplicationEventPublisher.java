package org.example;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ApplicationEventPublisher {
    Map<Class<?>, List<Listener>> eventListeners = new HashMap<>();

    protected void publishEvent(ApplicationEvent event) throws InvocationTargetException, IllegalAccessException {
        List<Listener> listenersList = eventListeners.get(event.getClass());
        if (listenersList == null) {
            return;
        }
        for (Listener listener : listenersList) {
            listener.invoke();
        }
    }
}

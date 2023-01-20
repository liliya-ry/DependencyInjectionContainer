package org.example.events;

import java.lang.reflect.Method;

public class Listener {
    Object eventListener;
    Method method;
    Class<?> eventType;

    public Listener(Object eventListener, Method method, Class<?> eventType) {
        this.eventListener = eventListener;
        this.method = method;
        this.eventType = eventType;
    }
}

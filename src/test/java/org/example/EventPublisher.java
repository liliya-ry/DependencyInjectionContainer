package org.example;

import org.example.annotations.Inject;

import java.lang.reflect.InvocationTargetException;

public class EventPublisher {
    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishStringEvent(final String message) throws InvocationTargetException, IllegalAccessException {
        System.out.println("Publishing custom String event with message " + message);
        CustomEvent customEvent = new CustomEvent(this, message);
        applicationEventPublisher.publishEvent(customEvent);
    }
}

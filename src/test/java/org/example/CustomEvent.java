package org.example;

import org.example.ApplicationEvent;

public class CustomEvent extends ApplicationEvent {
    private final String message;

    public CustomEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
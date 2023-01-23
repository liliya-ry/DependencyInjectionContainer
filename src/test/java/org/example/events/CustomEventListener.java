package org.example.events;

import org.example.annotations.Async;

public class CustomEventListener {
    @EventListener
    @Async
    public void handleStringEvent(CustomEvent event) {
        System.out.println("Received custom String event - " + event.getMessage());
    }
}

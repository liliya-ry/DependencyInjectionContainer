package org.example.events;

public class CustomEventListener {
    @EventListener
    public void handleStringEvent(CustomEvent event) {
        System.out.println("Received custom String event - " + event.getMessage());
    }
}

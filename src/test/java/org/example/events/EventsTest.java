package org.example.events;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.util.concurrent.*;

import org.example.Container;

class EventsTest {
    Container container;

    @BeforeEach
    void init() {
        container = new Container();
    }

    @Test
    void testPublisher() throws Exception {
        Executor executor = Executors.newFixedThreadPool(5);
        ApplicationEventMulticaster multicaster = new ApplicationEventMulticaster(executor);
        container.registerInstance("applicationEventMulticaster", multicaster);
        EventPublisher publisher = container.getInstance(EventPublisher.class);
        assertNotNull(publisher);
        CustomEventListener listener = container.getInstance(CustomEventListener.class);
        assertNotNull(listener);
        publisher.publishStringEvent("some message");
    }
}

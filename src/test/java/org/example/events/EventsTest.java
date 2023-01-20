package org.example.events;

import static org.junit.jupiter.api.Assertions.*;

import org.example.Container;
import org.junit.jupiter.api.*;

 class EventsTest {
    Container container;

    @BeforeEach
    void init() {
        container = new Container();
    }

    @Test
    void testPublisher() throws Exception {
        EventPublisher publisher = container.getInstance(EventPublisher.class);
        assertNotNull(publisher);
        CustomEventListener listener = container.getInstance(CustomEventListener.class);
        assertNotNull(listener);
        publisher.publishStringEvent("some message");
    }
}

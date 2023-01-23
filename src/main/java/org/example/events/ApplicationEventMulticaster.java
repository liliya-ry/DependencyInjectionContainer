package org.example.events;

import java.util.concurrent.Executor;

public class ApplicationEventMulticaster {
    Executor executor;

    public ApplicationEventMulticaster(Executor executor) {
        this.executor = executor;
    }
}

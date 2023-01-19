package org.example;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Listener {
    Method method;
    Object instance;

    Listener(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    void invoke() throws InvocationTargetException, IllegalAccessException {
        method.invoke(instance);
    }
}

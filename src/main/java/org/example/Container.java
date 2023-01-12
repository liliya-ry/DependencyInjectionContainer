package org.example;

import org.example.annotations.*;
import java.lang.reflect.*;
import java.util.*;
public class Container{
    private final Map<String, Object> namedInstances = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();

    public Object getInstance(String key) throws Exception {
        Object instance = namedInstances.get(key);

        if (instance == null) {
            registerInstance(key, instance);
        }
        return instance;
    }

    public <T> T getInstance(Class<T> c) throws Exception {
        T instance = (T) classInstances.get(c);

        if (instance == null) {
            instance = (T) createInstance(c);
            registerInstance(c, instance);
        }
        return instance;
    }

    public void registerInstance(String key, Object instance) throws Exception {
        decorateInstance(instance);
        namedInstances.put(key, instance);
    }

    public void registerInstance(Class<?> c, Object instance) throws Exception {
        decorateInstance(instance);
        classInstances.put(c, instance);
    }

    public void registerImplementation(Class<?> c, Class<?> subClass) throws Exception {

    }

    public void registerImplementation(Class<?> c) throws Exception {
        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            implementations.put(interfaceClass, c);
        }
    }

    public void registerInstance(Object instance) throws Exception {
        decorateInstance(instance);
        classInstances.put(instance.getClass(), instance);
    }

    public void decorateInstance(Object o) throws Exception {
        if (o == null) {
            return;
        }

        Class<?> instanceClass = o.getClass();
        injectConstructorsParams(o, instanceClass);
        injectFields(o, instanceClass);
    }

    private void injectFields(Object o, Class<?> instanceClass) throws Exception {
        Field[] fields = instanceClass.getDeclaredFields();
        for (Field field : fields) {
            Inject injectAnn = field.getAnnotation(Inject.class);
            if (injectAnn == null) {
                continue;
            }

            Named namedAnn = field.getAnnotation(Named.class);
            Object value = namedAnn == null ?
                    getInstance(field.getType()) :
                    getInstance(field.getName());
            field.setAccessible(true);
            field.set(o, value);
        }
    }

    private void injectConstructorsParams(Object o, Class<?> instanceClass) throws Exception {
        Constructor<?>[] constructors = instanceClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Inject injectAnn = constructor.getAnnotation(Inject.class);
            if (injectAnn == null) {
                continue;
            }
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                String paramName = parameter.getName();
                Field field = instanceClass.getField(paramName);
                Object value = classInstances.get(field.getClass());
                field.setAccessible(true);
                field.set(o, value);
            }
        }
    }

    private Object createInstance(Class<?> c) throws Exception {
        if (c.isInterface()) {
            Class<?> implClass = implementations.get(c);
            if (implClass == null) {
                Default defaultAnn = c.getAnnotation(Default.class);
                if (defaultAnn == null) {
                    return null;
                }
                implClass = defaultAnn.value();
                registerImplementation(c, implClass);
                c = implClass;
            }
        }
        Constructor<?> constructor = c.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}

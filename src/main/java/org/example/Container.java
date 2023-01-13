package org.example;

import org.example.annotations.*;
import org.example.exceptions.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NoInteractionsWanted;

import java.lang.reflect.*;
import java.util.*;

public class Container {
    private final Map<String, Object> namedInstances = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private final Properties properties;

    public Container(Properties properties) {
        this.properties = properties;
    }

    public Object getInstance(String key) {
        return namedInstances.get(key);
    }

    public <T> T getInstance(Class<T> c) throws Exception {
        T instance = (T) classInstances.get(c);

        if (instance == null) {
            instance = (T) createInstance(c);
            registerInstance(c, instance);
        }

        return instance;
    }

    public void registerInstance(String key, Object instance)  {
        namedInstances.put(key, instance);
    }

    public void registerInstance(Class<?> c, Object instance) throws Exception {
        Object existingInstance = classInstances.get(c);
        if (existingInstance != null) {
            throw new ConfigurationException("Instance for " + c.getSimpleName() + " already exists");
        }
        classInstances.put(c, instance);
    }

    public void registerInstance(Object instance)  {
        classInstances.put(instance.getClass(), instance);
    }

    public void registerImplementation(Class<?> c, Class<?> subClass)  {
        implementations.put(c, subClass);
    }

    public void registerImplementation(Class<?> c)  {
        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            registerImplementation(interfaceClass, c);
        }
    }

    private Object createInstance(Class<?> c) throws Exception {
        c = getClassForConstructor(c);
        if (c == null) {
            throw new ConfigurationException("Missing default interface implementation.");
        }

        Object instance = injectConstructor(c);
        if (instance == null) {
            Constructor<?> constructor = c.getDeclaredConstructor();
            instance = constructor.newInstance();
        }

        decorateInstance(instance);
        return instance;
    }

    public void decorateInstance(Object o) throws Exception {
        Class<?> instanceClass = o.getClass();
        Field[] fields = instanceClass.getDeclaredFields();
        for (Field field : fields) {
            Inject injectAnn = field.getAnnotation(Inject.class);
            if (injectAnn == null) {
                continue;
            }


            Object value = null;
            Lazy lazyAnn = field.getAnnotation(Lazy.class);
            if (lazyAnn != null) {
//                Class<?> fieldType = field.getType();
//                value = Mockito.spy(fieldType);
//                field.setAccessible(true);
//                field.set(o, value);
//                Mockito.verifyNoMoreInteractions(value);
//                continue;
            }

            Named namedAnn = field.getAnnotation(Named.class);
            value = namedAnn == null ?
                    getInstance(field.getType()) :
                    getInstance(field.getName());
            field.setAccessible(true);
            field.set(o, value);
        }

        if (o instanceof Initializer) {
            ((Initializer) o).init();
        }
    }

    private Class<?> getClassForConstructor(Class<?> c) throws Exception {
        if (!c.isInterface()) {
            return c;
        }

        Class<?> implClass = implementations.get(c);
        if (implClass != null) {
            return implClass;
        }

        Default defaultAnn = c.getAnnotation(Default.class);
        if (defaultAnn == null) {
            return null;
        }

        implClass = defaultAnn.value();
        registerImplementation(c, implClass);
        return implClass;
    }

    private Object injectConstructor(Class<?> instanceClass) throws Exception {
        boolean hasInjectAnn = false;
        Object instance = null;

        Constructor<?>[] constructors = instanceClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Inject injectAnn = constructor.getAnnotation(Inject.class);
            if (injectAnn == null) {
                continue;
            }

            if (hasInjectAnn) {
                throw new ConfigurationException("More than one constructor with @Inject annotation");
            }

            hasInjectAnn = true;
            Object[] values = getParamValues(constructor);
            instance = constructor.newInstance(values);
        }

        return instance;
    }

    private Object[] getParamValues(Constructor<?> constructor) throws Exception {
        Parameter[] parameters = constructor.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            if (!parameterType.isPrimitive() && !parameterType.equals(String.class)) {
                values[i] = getInstance(parameterType);
                continue;
            }

            Named namedAnn = parameters[i].getAnnotation(Named.class);
            if (namedAnn == null) {
                throw new ConfigurationException("Unnamed primitive parameter of type " + parameterType);
            }

            String propertyName = namedAnn.value();
            String property = properties.getProperty(propertyName);
            if (property == null) {
                throw new ConfigurationException("Missing property " + propertyName);
            }
            values[i] = fromStringToPrimitive(property, parameterType);
        }

        return values;
    }

    private Object fromStringToPrimitive(String s, Class<?> type) {
        return switch(type.getSimpleName()) {
            case "int" -> Integer.parseInt(s);
            case "double" -> Double.parseDouble(s);
            case "float" -> Float.parseFloat(s);
            case "boolean" -> Boolean.parseBoolean(s);
            case "short" -> Short.parseShort(s);
            case "long" -> Long.parseLong(s);
            case "byte" -> Byte.parseByte(s);
            case "char" -> s.charAt(0);
            default -> s;
        };
    }
}

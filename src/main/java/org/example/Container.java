package org.example;

import org.example.annotations.*;
import org.example.exceptions.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

public class Container {
    private final Map<String, Object> namedInstances = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private Set<Class<?>> visitedClasses = new HashSet<>();

    public Container(Properties properties) {
        properties.forEach((k, v) -> namedInstances.put((String) k, v));
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

    public void registerInstance(String key, Object instance) throws Exception {
        Object existingInstance = namedInstances.get(key);

        if (existingInstance != null) {
            throw new ConfigurationException("Instance with name " + key + " already exists");
        }

        namedInstances.put(key, instance);
    }

    public void registerInstance(Class<?> c, Object instance) throws Exception {
        Object existingInstance = classInstances.get(c);

        if (existingInstance != null && !instance.getClass().equals(c)) {
            throw new ConfigurationException("Instance for " + c.getSimpleName() + " already exists");
        }

        classInstances.put(instance.getClass(), instance);
    }

    public void registerInstance(Object instance) throws Exception {
        registerInstance(instance.getClass(), instance);
    }

    public void registerImplementation(Class<?> c, Class<?> subClass) throws Exception {
        if (!c.isAssignableFrom(subClass)) {
            throw new ConfigurationException(c + " is not assignable from " + subClass);
        }
        implementations.put(c, subClass);
    }

    public void registerImplementation(Class<?> c) throws Exception {
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
            decorateInstance(instance);
        }

        visitedClasses = new HashSet<>();

        return instance;
    }

    public void decorateInstance(Object o) throws Exception {
        Class<?> instanceClass = o.getClass();
        boolean hasCircularDependency = false;
        if (visitedClasses.contains(instanceClass)) {
            hasCircularDependency = true;
            //throw new ConfigurationException("Circular dependency detected in class " + instanceClass);
        }

        visitedClasses.add(instanceClass);
        injectFields(o, hasCircularDependency);

        if (o instanceof Initializer) {
            ((Initializer) o).init();
        }
    }

    private void injectFields(Object o, boolean hasCircularDependency) throws Exception {
        Class<?> instanceClass = o.getClass();

        Field[] fields = instanceClass.getDeclaredFields();
        for (Field field : fields) {
            Inject injectAnn = field.getAnnotation(Inject.class);
            if (injectAnn == null) {
                continue;
            }

            Class<?> fieldType = field.getType();
            Lazy lazyAnn = field.getAnnotation(Lazy.class);
            if (lazyAnn != null || hasCircularDependency) {
                setMock(o, field, fieldType);
                continue;
            }

            Named namedAnn = field.getAnnotation(Named.class);
            Object value = namedAnn != null ? getInstance(field.getName()) : getInstance(fieldType);
            field.setAccessible(true);
            field.set(o, value);
        }
    }

    private void setMock(Object o, Field field, Class<?> fieldType) throws IllegalAccessException, InvocationTargetException {
        Object value = Mockito.mock(fieldType);
        for (Method method : fieldType.getDeclaredMethods()) {
            Mockito.when(method.invoke(value)).then(new ReplaceMockWithOriginalAnswer(o, field, value));
        }
        field.setAccessible(true);
        field.set(o, value);
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
        if (!c.isAssignableFrom(implClass)) {
            throw new ConfigurationException(c + "is not assignable from " + implClass);
        }

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

            //Lazy lazyAnn = constructor.getAnnotation(Lazy.class);

            hasInjectAnn = true;
            Object[] values = getParamValues(constructor);
            instance = constructor.newInstance(values);
        }

        return instance;
    }

    private static final Set<Class<?>> WRAPPER_CLASSES = Set.of(
            Integer.class, Float.class, Double.class, Short.class,
            Long.class, Byte.class, Character.class, Boolean.class
    );

    private Object[] getParamValues(Constructor<?> constructor) throws Exception {
        Parameter[] parameters = constructor.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            if (isNotWrapperPrimitiveOrString(parameterType)) {
                values[i] = getInstance(parameterType);
                continue;
            }

            Named namedAnn = parameters[i].getAnnotation(Named.class);
            if (namedAnn == null) {
                throw new ConfigurationException("Unnamed primitive parameter of type " + parameterType);
            }

            String propertyName = namedAnn.value();
            String property = (String) namedInstances.get(propertyName);

            if (property == null) {
                throw new ConfigurationException("Missing property " + propertyName);
            }

            values[i] = fromStringToPrimitive(property, parameterType);
        }

        return values;
    }

    private boolean isNotWrapperPrimitiveOrString(Class<?> type) {
        return !type.isPrimitive() && !WRAPPER_CLASSES.contains(type) && !type.equals(String.class);
    }

    private Object fromStringToPrimitive(String s, Class<?> type) {
        return switch(type.getSimpleName()) {
            case "int", "Integer" -> Integer.parseInt(s);
            case "double", "Double" -> Double.parseDouble(s);
            case "float", "Float" -> Float.parseFloat(s);
            case "boolean", "Boolean" -> Boolean.parseBoolean(s);
            case "short", "Short" -> Short.parseShort(s);
            case "long", "Long" -> Long.parseLong(s);
            case "byte", "Byte" -> Byte.parseByte(s);
            case "char", "Char" -> s.charAt(0);
            default -> s;
        };
    }

    private class ReplaceMockWithOriginalAnswer implements Answer<Object> {
        private final Class<?> fieldType;
        Object o;
        Object fieldValue;
        boolean isCalled = false;
        Field field;


        public ReplaceMockWithOriginalAnswer(Object o, Field field, Object fieldValue) {
            this.o = o;
            this.field = field;
            this.fieldValue = fieldValue;
            this.fieldType = fieldValue.getClass();
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            if (isCalled) {
                return invocation.callRealMethod();
            }

            isCalled = true;
            fieldValue = createInstance(fieldType);
            classInstances.put(fieldType, fieldValue);
            field.setAccessible(true);
            field.set(o, fieldValue);
            return invocation.callRealMethod();
        }
    }
}
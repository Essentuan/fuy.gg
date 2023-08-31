package com.busted_moments.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Reflection {
    public static boolean hasConstructor(Class<?> clazz, Class<?>... args) {
        try {
            clazz.getDeclaredConstructor(args);

            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean hasMethod(Class<?> clazz, String method, Class<?>... args) {
        try {
            clazz.getDeclaredMethod(method, args);

            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static Optional<Method> getMethod(Class<?> clazz, String method, Class<?>... args) {
        try {
            return Optional.of(clazz.getDeclaredMethod(method, args));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static String getDescriptor(Class<?> clazz) {
        return clazz.descriptorString();
    }

    public static String getDescriptor(Method method) {
        StringBuilder descriptor = new StringBuilder("(");

        for (final Class<?> c : method.getParameterTypes()) {
            descriptor.append(c.descriptorString());
        }
        descriptor.append(')');

        return descriptor + method.getReturnType().descriptorString();
    }

    public static String getUID(Method method) {
        return "%s%s%s".formatted(method.getDeclaringClass().descriptorString(), method.getName(), getDescriptor(method));
    }

    public static String getUID(Method method, @NotNull Object instance) {
        return "%s%s%s@%s".formatted(method.getDeclaringClass().descriptorString(), method.getName(), getDescriptor(method), instance.hashCode());
    }


    public static boolean isStatic(Executable executable) {
        return Modifier.isStatic(executable.getModifiers());
    }

    public static boolean isStatic(Field executable) {
        return Modifier.isStatic(executable.getModifiers());
    }

    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    private static final Map<Integer, Field> FIELD_MAP = new ConcurrentHashMap<>();

    public static Field getField(String field, Class<?> clazz) {
        int hash = Objects.hash(field, clazz);
        Field f = FIELD_MAP.get(hash);

        if (f == null) {
            try {
                f = clazz.getDeclaredField(field);
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }

            FIELD_MAP.put(hash, f);
        }

        return f;
    }

    @SuppressWarnings("unchecked")
    public static <T, C> T get(String field, Class<C> clazz, @Nullable C instance) {
        try {
            return (T) getField(field, clazz).get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, C> void set(String field, Class<C> clazz, @Nullable C instance, T value) {
        try {
            getField(field, clazz).set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Integer, Method> METHOD_MAP = new ConcurrentHashMap<>();

    public static Method getMethod(String method, Class<?> clazz, Class<?>... parameterTypes) {
        int hash = Objects.hash(method, clazz, Arrays.hashCode(parameterTypes));
        Method m = METHOD_MAP.get(hash);

        if (m == null) {
            try {
                m = clazz.getDeclaredMethod(method, parameterTypes);
                m.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

           METHOD_MAP.put(hash, m);
        }

        return m;
    }

    @SuppressWarnings("unchecked")
    public static <C, R> R invoke(String method, Class<C> clazz, C instance, Object... args) {
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();

        try {
            return (R) getMethod(method, clazz, types).invoke(instance, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
           throw new RuntimeException(e);
        }
    }


    public static String toSimpleString(Class<?> clazz) {
        StringBuilder builder = new StringBuilder(clazz.getSimpleName());

        Class<?> enclosing = clazz.getEnclosingClass();
        while (enclosing != null) {
            builder.insert(0, enclosing.getSimpleName() + ".");
            enclosing = enclosing.getEnclosingClass();
        }

        return builder.toString();
    }
}

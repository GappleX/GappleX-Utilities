/*
 * Copyright (C) 2026 GappleX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.gapplex.gxu.reflect;

import io.github.gapplex.gxu.convert.PrimitiveUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Fields {

    private Fields() {
        throw new AssertionError("No instances");
    }

    public static Object getField(Object instance, String name) {
        return getField(instance, name, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object instance, String name, Class<T> type) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Class<?> owner = instance.getClass();
        Field field = requireField(owner, name, false);
        checkReadable(owner, field, type);
        return (T) read(instance, field);
    }

    public static void setField(Object instance, String name, Object value) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(name, "name");
        Class<?> owner = instance.getClass();
        Field field = requireField(owner, name, false);
        checkWritable(owner, field, value);
        write(instance, field, value);
    }

    public static Object getStaticField(Class<?> owner, String name) {
        return getStaticField(owner, name, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getStaticField(Class<?> owner, String name, Class<T> type) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Field field = requireField(owner, name, true);
        checkReadable(owner, field, type);
        return (T) read(null, field);
    }

    public static void setStaticField(Class<?> owner, String name, Object value) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
        Field field = requireField(owner, name, true);
        checkWritable(owner, field, value);
        write(null, field, value);
    }

    public static Field findField(Class<?> type, String name) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(name, "name");
        return lookup(type, name);
    }

    private static @NotNull Field requireField(Class<?> owner, String name, boolean staticField) {
        Field field = lookup(owner, name);
        if (field == null) {
            throw ReflectException.fieldNotFound(owner.getName(), name);
        }
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (staticField && !isStatic) {
            throw ReflectException.instanceFieldNotSupported(owner.getName(), name);
        }
        if (!staticField && isStatic) {
            throw ReflectException.staticFieldNotSupported(owner.getName(), name);
        }
        return field;
    }

    private static @Nullable Field lookup(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            Field found = findDeclared(current, name);
            if (found != null) {
                return found;
            }
            Field fromInterface = lookupInterfaces(current, name, null);
            if (fromInterface != null) {
                return fromInterface;
            }
        }
        return null;
    }

    private static @Nullable Field lookupInterfaces(Class<?> type, String name, Set<Class<?>> visited) {
        Class<?>[] ifaces = type.getInterfaces();
        if (ifaces.length == 0) {
            return null;
        }
        if (visited == null) {
            visited = Collections.newSetFromMap(new IdentityHashMap<>());
        }
        for (Class<?> iface : ifaces) {
            if (!visited.add(iface)) {
                continue;
            }
            Field f = findDeclared(iface, name);
            if (f != null) {
                return f;
            }
            Field nested = lookupInterfaces(iface, name, visited);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private static Field findDeclared(Class<?> type, String name) {
        return FIELD_INDEX.get(type).get(name);
    }

    private static void checkReadable(Class<?> owner, @NotNull Field field, Class<?> type) {
        Class<?> expected = PrimitiveUtils.wrap(type);
        Class<?> actual = PrimitiveUtils.wrap(field.getType());
        if (!expected.isAssignableFrom(actual)) {
            throw ReflectException.typeMismatch(
                    owner.getName(), field.getName(), type, field.getType());
        }
    }

    private static void checkWritable(Class<?> owner, Field field, Object value) {
        if (value == null) {
            if (field.getType().isPrimitive()) {
                throw ReflectException.nullForPrimitive(owner.getName(), field.getName());
            }
            return;
        }
        Class<?> expected = PrimitiveUtils.wrap(field.getType());
        if (!expected.isAssignableFrom(value.getClass())) {
            throw ReflectException.typeMismatch(
                    owner.getName(), field.getName(), field.getType(), value.getClass());
        }
    }

    private static Object read(Object instance, Field field) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException | SecurityException e) {
            throw ReflectException.accessDenied(
                    field.getDeclaringClass().getName(), field.getName(), e);
        } catch (Throwable t) {
            throw wrap(t);
        }
    }

    private static void write(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException | SecurityException e) {
            throw ReflectException.accessDenied(
                    field.getDeclaringClass().getName(), field.getName(), e);
        } catch (Throwable t) {
            throw wrap(t);
        }
    }

    @Contract(value = "null -> new", pure = true)
    private static @NotNull RuntimeException wrap(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new RuntimeException(t);
    }

    private static final ClassValue<Map<String, Field>> FIELD_INDEX =
            new ClassValue<>() {
                @Override
                protected @NotNull Map<String, Field> computeValue(@NotNull Class<?> type) {
                    Field[] fields;
                    try {
                        fields = type.getDeclaredFields();
                    } catch (Throwable ignored) {
                        return Collections.emptyMap();
                    }
                    if (fields.length == 0) {
                        return Collections.emptyMap();
                    }
                    Map<String, Field> map =
                            new HashMap<>(Math.max(4, fields.length * 2));
                    for (Field f : fields) {
                        if (f != null) {
                            map.putIfAbsent(f.getName(), f);
                        }
                    }
                    return map;
                }
            };

    @Contract(pure = true)
    @Deprecated
    public static @NotNull String getVersion() {
        return "SAFE";
    }
}
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
package io.github.gapplex.gxu.unsafe;

import io.github.gapplex.gxu.convert.ConvertException;
import io.github.gapplex.gxu.convert.ConvertUtils;
import io.github.gapplex.gxu.convert.PrimitiveUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Unsafeties {

    private static final ConcurrentHashMap<IUnsafe, Unsafeties> CACHE = new ConcurrentHashMap<>();
    private static final int MAX_DEPTH = 64;
    private final IUnsafe unsafe;

    private final ClassValue<Map<String, Field>> nameCache = new ClassValue<>() {
        @Override
        protected Map<String, Field> computeValue(@NotNull Class<?> type) {
            Map<String, Field> map = new LinkedHashMap<>();
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (!map.containsKey(f.getName())) {
                        map.put(f.getName(), f);
                    }
                }
            }
            return map;
        }
    };

    private final ClassValue<Map<Field, FieldInfo>> infoCache = new ClassValue<>() {
        @Override
        protected Map<Field, FieldInfo> computeValue(@NotNull Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    private final ClassValue<ArrayLayout> arrayCache = new ClassValue<>() {
        @Override
        protected ArrayLayout computeValue(Class<?> type) {
            if (!type.isArray()) return null;
            return new ArrayLayout(
                    unsafe.arrayBaseOffset(type),
                    unsafe.arrayIndexScale(type),
                    type.getComponentType());
        }
    };

    private Unsafeties(IUnsafe unsafe) {
        this.unsafe = unsafe;
    }

    public static Unsafeties getInstance(IUnsafe unsafe) {
        return CACHE.computeIfAbsent(Objects.requireNonNull(unsafe, "unsafe"), Unsafeties::new);
    }

    public BeanMap beanToMap(Object instance) {
        Objects.requireNonNull(instance, "instance");
        Class<?> clazz = instance.getClass();
        if (isSimpleType(clazz) || clazz.isArray()) {
            throw new IllegalArgumentException("Cannot serialize primitive/simple type or array as a bean: " + clazz.getName());
        }

        BeanMap out = new BeanMap();
        Map<String, Field> fields = nameCache.get(clazz);

        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            Field field = entry.getValue();
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
            if (field.isAnnotationPresent(Ignore.class)) continue;

            FieldInfo fi = info(field);
            Object rawValue = readAt(unsafe, instance, fi.offset, fi.type);
            out.put(entry.getKey(), convertToMapValue(rawValue));
        }
        return out;
    }

    private Object convertToMapValue(Object value) {
        if (value == null) return null;
        Class<?> clazz = value.getClass();

        if (isSimpleType(clazz)) {
            return value;
        }
        if (clazz.isArray()) {
            int len = Array.getLength(value);
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(convertToMapValue(Array.get(value, i)));
            }
            return list;
        }
        if (value instanceof Collection) {
            Collection<?> src = (Collection<?>) value;
            List<Object> list = new ArrayList<>(src.size());
            for (Object item : src) {
                list.add(convertToMapValue(item));
            }
            return list;
        }
        if (value instanceof Map) {
            Map<?, ?> src = (Map<?, ?>) value;
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : src.entrySet()) {
                map.put(String.valueOf(entry.getKey()), convertToMapValue(entry.getValue()));
            }
            return map;
        }
        return beanToMap(value);
    }

    private static boolean isSimpleType(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || clazz.isEnum()
                || clazz.isPrimitive()
                || clazz == Class.class;
    }

    public <T> T deserialize(Class<T> target, Map<String, Object> fields) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(fields, "fields");
        Object instance;
        try {
            instance = unsafe.allocateInstance(target);
        } catch (InstantiationException ex) {
            throw UnsafeException.accessDenied(target.getName(), "Cannot allocate instance of " + target.getName(), ex);
        }
        fill(instance, target, fields, 0);
        @SuppressWarnings("unchecked")
        T result = (T) instance;
        return result;
    }

    private void fill(Object instance, Class<?> target, Map<String, Object> fields, int depth) {
        Map<String, Field> byName = nameCache.get(target);
        for (Map.Entry<String, Field> e : byName.entrySet()) {
            Field field = e.getValue();
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
            if (field.isAnnotationPresent(Ignore.class)) continue;

            boolean notNull = field.isAnnotationPresent(io.github.gapplex.gxu.unsafe.NotNull.class);
            boolean hasKey = fields.containsKey(e.getKey());
            Object rawValue = fields.get(e.getKey());

            if (!hasKey) {
                if (notNull) {
                    String path = target.getName() + '.' + e.getKey();
                    throw new UnsafeException(UnsafeException.Reason.NULL_FOR_NOTNULL, path,
                            "Field '" + e.getKey() + "' annotated as NotNull is missing from source map.", null);
                }
                continue;
            }

            if (rawValue == null && notNull) {
                String path = target.getName() + '.' + e.getKey();
                throw new UnsafeException(UnsafeException.Reason.NULL_FOR_NOTNULL, path,
                        "Field '" + e.getKey() + "' annotated as NotNull received null.", null);
            }

            FieldInfo fi = info(field);
            Object converted = convert(rawValue, field.getGenericType(), depth + 1, fi.path);
            writeField(instance, field, fi, converted);
        }
        unsafe.storeFence();
    }

    private Object convert(Object value, Type targetType, int depth, String path) {
        if (depth > MAX_DEPTH) {
            throw UnsafeException.nestingTooDeep(path, MAX_DEPTH);
        }
        if (value == null) return null;

        Class<?> raw = rawClass(targetType, path);

        if (raw.isArray() && value instanceof List) {
            return convertArray((List<?>) value, raw.getComponentType(), depth, path);
        }
        if (Collection.class.isAssignableFrom(raw) && value instanceof List) {
            Type elemType = genericType(targetType, 0);
            return convertCollection((List<?>) value, raw, elemType, depth, path);
        }

        // 如果目标明确是 Map，那么把 BeanMap 和普通 Map 都当做普通 Map 处理
        if (Map.class.isAssignableFrom(raw)) {
            if (value instanceof Map) {
                Type keyType = genericType(targetType, 0);
                Type valType = genericType(targetType, 1);
                return convertMap((Map<?, ?>) value, keyType, valType, depth, path);
            }
            throw UnsafeException.typeMismatch(path, raw, value.getClass());
        }

        if (raw == Object.class) {
            return value;
        }

        // 目标是具体对象（非 Map 接口）
        if (value instanceof Map) {
            if (value instanceof BeanMap) {
                // 强制视为 Bean 来反序列化
                if (!raw.isInterface()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cast = (Map<String, Object>) value;
                    Object nested;
                    try {
                        nested = unsafe.allocateInstance(raw);
                    } catch (InstantiationException ex) {
                        throw UnsafeException.accessDenied(path, "Cannot allocate instance of " + raw.getName(), ex);
                    }
                    fill(nested, raw, cast, depth);
                    return nested;
                } else {
                    throw UnsafeException.accessDenied(path, "Cannot instantiate interface " + raw.getName(), null);
                }
            } else {
                // 普通 Map 严格视为普通 Map，无法赋值给 Bean 字段
                throw UnsafeException.typeMismatch(path, raw, value.getClass());
            }
        }

        try {
            return ConvertUtils.convert(value, raw);
        } catch (ConvertException ex) {
            throw UnsafeException.conversionFailed(path, ex);
        }
    }

    private Object convertArray(List<?> list, Class<?> component, int depth, String path) {
        Object arr = Array.newInstance(component, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object item = convert(list.get(i), component, depth + 1, path + "[" + i + "]");
            try {
                Array.set(arr, i, item);
            } catch (IllegalArgumentException ex) {
                throw UnsafeException.typeMismatch(path + "[" + i + "]", component, item == null ? null : item.getClass());
            }
        }
        return arr;
    }

    private Object convertCollection(List<?> list, Class<?> raw, Type elementType, int depth, String path) {
        Collection<Object> out = newCollection(raw, path);
        int i = 0;
        for (Object item : list) {
            out.add(convert(item, elementType, depth + 1, path + "[" + i++ + "]"));
        }
        return out;
    }

    private Object convertMap(Map<?, ?> src, Type keyType, Type valType, int depth, String path) {
        Map<Object, Object> out = new LinkedHashMap<>(src.size());
        for (Map.Entry<?, ?> entry : src.entrySet()) {
            Object key = convert(entry.getKey(), keyType, depth + 1, path + "<key>");
            Object val = convert(entry.getValue(), valType, depth + 1, path + "<value>");
            out.put(key, val);
        }
        return out;
    }

    // ==================== 工具方法 ====================

    private static Type genericType(Type genericType, int index) {
        if (genericType instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
            if (index < args.length) {
                return args[index];
            }
        }
        return Object.class;
    }

    private static Class<?> rawClass(Type type, String path) {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) return (Class<?>) ((ParameterizedType) type).getRawType();
        if (type instanceof GenericArrayType) {
            Class<?> comp = rawClass(((GenericArrayType) type).getGenericComponentType(), path);
            return Array.newInstance(comp, 0).getClass();
        }
        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return bounds.length == 0 ? Object.class : rawClass(bounds[0], path);
        }
        if (type instanceof WildcardType) {
            Type[] upper = ((WildcardType) type).getUpperBounds();
            return upper.length == 0 ? Object.class : rawClass(upper[0], path);
        }
        throw UnsafeException.unsupportedType(path, type.getTypeName());
    }

    private static Collection<Object> newCollection(Class<?> raw, String path) {
        if (raw.isAssignableFrom(ArrayList.class)) return new ArrayList<>();
        if (raw.isAssignableFrom(LinkedHashSet.class)) return new LinkedHashSet<>();
        if (raw.isAssignableFrom(Collection.class)) return new ArrayList<>();
        throw UnsafeException.unsupportedCollection(path, raw);
    }

    private FieldInfo info(Field field) {
        Objects.requireNonNull(field, "field");
        return infoCache.get(field.getDeclaringClass()).computeIfAbsent(field, this::computeInfo);
    }

    private FieldInfo computeInfo(Field f) {
        boolean isStatic = Modifier.isStatic(f.getModifiers());
        long offset = isStatic ? unsafe.staticFieldOffset(f) : unsafe.objectFieldOffset(f);
        Object staticBase = isStatic ? unsafe.staticFieldBase(f) : null;
        String path = f.getDeclaringClass().getName() + "." + f.getName();
        return new FieldInfo(offset, f.getType(), staticBase, isStatic, path);
    }

    private void writeField(Object instance, Field field, FieldInfo fi, Object value) {
        if (!fi.isStatic) {
            if (instance == null) throw new NullPointerException("instance is null for field " + fi.path);
            if (!field.getDeclaringClass().isInstance(instance)) {
                throw UnsafeException.baseTypeMismatch(fi.path, field.getDeclaringClass(), instance.getClass());
            }
        }
        Class<?> declared = fi.type;
        if (value == null) {
            if (declared.isPrimitive()) {
                throw UnsafeException.nullForPrimitive(field.getDeclaringClass().getName(), field.getName());
            }
        } else {
            boolean compatible = declared.isPrimitive()
                    ? PrimitiveUtils.wrap(declared) == value.getClass()
                    : declared.isInstance(value);
            if (!compatible) {
                throw UnsafeException.typeMismatch(fi.path, declared, value.getClass());
            }
        }
        writeAt(unsafe, fi.isStatic ? fi.staticBase : instance, fi.offset, fi.type, value);
    }

    // ==================== 底层读写 ====================

    private static void checkIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    private static Object readAt(IUnsafe u, Object base, long offset, Class<?> type) {
        if (type == int.class) return u.getInt(base, offset);
        if (type == long.class) return u.getLong(base, offset);
        if (type == boolean.class) return u.getBoolean(base, offset);
        if (type == byte.class) return u.getByte(base, offset);
        if (type == short.class) return u.getShort(base, offset);
        if (type == char.class) return u.getChar(base, offset);
        if (type == float.class) return u.getFloat(base, offset);
        if (type == double.class) return u.getDouble(base, offset);
        return u.getObject(base, offset);
    }

    private static void writeAt(IUnsafe u, Object base, long offset, Class<?> type, Object value) {
        if (type == int.class) u.putInt(base, offset, (Integer) value);
        else if (type == long.class) u.putLong(base, offset, (Long) value);
        else if (type == boolean.class) u.putBoolean(base, offset, (Boolean) value);
        else if (type == byte.class) u.putByte(base, offset, (Byte) value);
        else if (type == short.class) u.putShort(base, offset, (Short) value);
        else if (type == char.class) u.putChar(base, offset, (Character) value);
        else if (type == float.class) u.putFloat(base, offset, (Float) value);
        else if (type == double.class) u.putDouble(base, offset, (Double) value);
        else u.putObject(base, offset, value);
    }

    private static final class ArrayLayout {
        final int base;
        final int scale;
        final Class<?> component;

        ArrayLayout(int base, int scale, Class<?> component) {
            this.base = base;
            this.scale = scale;
            this.component = component;
        }
    }

    private static final class FieldInfo {
        final long offset;
        final Class<?> type;
        final Object staticBase;
        final boolean isStatic;
        final String path;

        FieldInfo(long offset, Class<?> type, Object staticBase, boolean isStatic, String path) {
            this.offset = offset;
            this.type = type;
            this.staticBase = staticBase;
            this.isStatic = isStatic;
            this.path = path;
        }
    }
}
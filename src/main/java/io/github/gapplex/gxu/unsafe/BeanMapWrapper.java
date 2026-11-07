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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public final class BeanMapWrapper {

    private BeanMapWrapper() {
        throw new AssertionError("No instances");
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(Class<?> targetClass, Object rawData) {
        return (T) doWrap(targetClass, targetClass, rawData);
    }

    private static Object doWrap(Class<?> rawClass, Type targetType, Object rawData) {
        if (rawData == null) {
            return null;
        }
        if (targetType == null) {
            return rawData;
        }

        if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            Type rawType = pt.getRawType();
            if (rawType instanceof Class) {
                Class<?> ptClass = (Class<?>) rawType;
                if (Collection.class.isAssignableFrom(ptClass) && rawData instanceof List) {
                    Type elemType = pt.getActualTypeArguments()[0];
                    return wrapList(elemType, (List<?>) rawData);
                }
                if (Map.class.isAssignableFrom(ptClass) && rawData instanceof Map) {
                    Type valType = pt.getActualTypeArguments()[1];
                    return wrapMap(valType, (Map<?, ?>) rawData);
                }
            }
        }

        if (rawClass == null || rawClass == Object.class || isSimpleType(rawClass)) {
            return rawData;
        }

        if (rawClass.isArray() && rawData instanceof List) {
            return wrapList(rawClass.getComponentType(), (List<?>) rawData);
        }
        if (Collection.class.isAssignableFrom(rawClass) && rawData instanceof List) {
            return wrapList(Object.class, (List<?>) rawData);
        }

        if (Map.class.isAssignableFrom(rawClass)) {
            return rawData;
        }

        if (rawData instanceof Map) {
            return wrapBean(rawClass, (Map<?, ?>) rawData);
        }

        return rawData;
    }

    private static BeanMap wrapBean(Class<?> clazz, Map<?, ?> rawMap) {
        BeanMap beanMap = new BeanMap();
        Map<String, Field> fields = getFields(clazz);

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();

            Field field = fields.get(key);
            if (field != null) {
                beanMap.put(key, doWrap(field.getType(), field.getGenericType(), value));
            } else {
                beanMap.put(key, value);
            }
        }
        return beanMap;
    }

    private static List<Object> wrapList(Type elemType, List<?> rawList) {
        List<Object> list = new ArrayList<>(rawList.size());
        Class<?> rawClass = extractRawClass(elemType);
        for (Object item : rawList) {
            list.add(doWrap(rawClass, elemType, item));
        }
        return list;
    }

    private static Map<Object, Object> wrapMap(Type valType, Map<?, ?> rawMap) {
        Map<Object, Object> map = new LinkedHashMap<>();
        Class<?> rawClass = extractRawClass(valType);
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            map.put(entry.getKey(), doWrap(rawClass, valType, entry.getValue()));
        }
        return map;
    }

    private static final ClassValue<Map<String, Field>> FIELD_CACHE = new ClassValue<>() {
        @Override
        protected Map<String, Field> computeValue(@NotNull Class<?> type) {
            Map<String, Field> map = new LinkedHashMap<>();
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    int mod = f.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || f.isSynthetic()) {
                        continue;
                    }
                    map.putIfAbsent(f.getName(), f);
                }
            }
            return Collections.unmodifiableMap(map);
        }
    };

    private static Map<String, Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.get(clazz);
    }

    private static Class<?> extractRawClass(Type type) {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) return (Class<?>) ((ParameterizedType) type).getRawType();
        if (type instanceof GenericArrayType) {
            Class<?> comp = extractRawClass(((GenericArrayType) type).getGenericComponentType());
            return Array.newInstance(comp, 0).getClass();
        }
        if (type instanceof WildcardType) {
            Type[] upper = ((WildcardType) type).getUpperBounds();
            return upper.length == 0 ? Object.class : extractRawClass(upper[0]);
        }
        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return bounds.length == 0 ? Object.class : extractRawClass(bounds[0]);
        }
        return Object.class;
    }

    private static boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || clazz == Boolean.class
                || clazz == Character.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Date.class
                || clazz == Calendar.class
                || clazz.isEnum()
                || java.time.temporal.TemporalAccessor.class.isAssignableFrom(clazz);
    }
}
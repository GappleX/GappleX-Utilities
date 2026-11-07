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
package io.github.gapplex.gxu.container;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

public class Maps {
    @SuppressWarnings("rawtypes")
    private static final ReflectUtils<Map> reflectUtils = new ReflectUtils<>(new CtorClassValue<>(Map.class));

    private Maps(){}

    public static <K, V, R extends Map<K, V>> R modifierToAnyMap(
            @NotNull MapModifier<K, V> modifier,
            @NotNull Function<? super Map<K, V>, ? extends R> function) {
        return function.apply(Collections.unmodifiableMap(modifier.getRawMap()));
    }

    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull MapBuilder mapOf(Class<? extends Map> clazz, boolean immutable){
        return new MapBuilder(reflectUtils.createReflectFunc(clazz), immutable);
    }

    @Contract("_ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull MapBuilder mapOf(Class<? extends Map> clazz){
        return mapOf(clazz, false);
    }

    @SuppressWarnings("rawtypes")
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull MapBuilder mapOf(IntFunction<Map> func, boolean immutable){
        return new MapBuilder(func, immutable);
    }

    @SuppressWarnings("rawtypes")
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MapBuilder mapOf(IntFunction<Map> func){
        return mapOf(func, false);
    }

    @Contract(pure = true)
    public static <K, V> @Unmodifiable @NotNull Map<K, V> of() {
        return Collections.emptyMap();
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1) {
        Map<K, V> map = newLinkedHashMap(1);
        safePut(map, k1, v1);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2) {
        Map<K, V> map = newLinkedHashMap(2);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3) {
        Map<K, V> map = newLinkedHashMap(3);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4) {
        Map<K, V> map = newLinkedHashMap(4);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5) {
        Map<K, V> map = newLinkedHashMap(5);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        safePut(map, k5, v5);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                                                                 @NotNull K k6, @NotNull V v6) {
        Map<K, V> map = newLinkedHashMap(6);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        safePut(map, k5, v5);
        safePut(map, k6, v6);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                                                                 @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7) {
        Map<K, V> map = newLinkedHashMap(7);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        safePut(map, k5, v5);
        safePut(map, k6, v6);
        safePut(map, k7, v7);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                                                                 @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8) {
        Map<K, V> map = newLinkedHashMap(8);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        safePut(map, k5, v5);
        safePut(map, k6, v6);
        safePut(map, k7, v7);
        safePut(map, k8, v8);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                                                                 @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8, @NotNull K k9, @NotNull V v9) {
        Map<K, V> map = newLinkedHashMap(9);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        safePut(map, k5, v5);
        safePut(map, k6, v6);
        safePut(map, k7, v7);
        safePut(map, k8, v8);
        safePut(map, k9, v9);
        return unmodifiableMap(map);
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                                                                 @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8, @NotNull K k9, @NotNull V v9, @NotNull K k10, @NotNull V v10) {
        Map<K, V> map = newLinkedHashMap(10);
        safePut(map, k1, v1);
        safePut(map, k2, v2);
        safePut(map, k3, v3);
        safePut(map, k4, v4);
        safePut(map, k5, v5);
        safePut(map, k6, v6);
        safePut(map, k7, v7);
        safePut(map, k8, v8);
        safePut(map, k9, v9);
        safePut(map, k10, v10);
        return unmodifiableMap(map);
    }

    @SafeVarargs
    public static <K, V> @UnmodifiableView @NotNull Map<K, V> ofEntries(Map.Entry<? extends K, ? extends V> @NotNull ... entries) {
        Map<K, V> map = newLinkedHashMap(entries.length);
        for (Map.Entry<? extends K, ? extends V> e : entries) {
            safePut(map, e.getKey(), e.getValue());
        }
        return unmodifiableMap(map);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <K, V> Map.@NotNull Entry<K, V> entry(@NotNull K k, @NotNull V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    @Contract("_ -> new")
    public static <K, V> @NotNull HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(CapacityUtils.capacity(expectedSize));
    }

    @Contract("_ -> new")
    public static <K, V> @NotNull LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
        return new LinkedHashMap<>(CapacityUtils.capacity(expectedSize));
    }

    public static <K, V> @UnmodifiableView @NotNull Map<K, V> unmodifiableMap(@NotNull Map<K, V> map) {
        return Collections.unmodifiableMap(Objects.requireNonNull(map, "map"));
    }

    public static final class MapBuilder{
        @SuppressWarnings("rawtypes")
        private final IntFunction<Map> create;
        private final boolean immutable;

        @SuppressWarnings("rawtypes")
        public MapBuilder(IntFunction<Map> create, boolean immutable) {
            this.create = Objects.requireNonNull(create, "create");
            this.immutable = immutable;
        }

        @SafeVarargs
        public final <K, V> Map<K, V> ofEntries(Map.Entry<? extends K, ? extends V> @NotNull ... entries) {
            Map<K, V> map = newMapInstance(entries.length);
            for (Map.Entry<? extends K, ? extends V> e : entries) {
                safePut(map, e.getKey(), e.getValue());
            }
            return finish(map);
        }

        public <K, V> Map<K, V> of(){
            return finish(newMapInstance(0));
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1) {
            Map<K, V> map = newMapInstance(1);
            safePut(map, k1, v1);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2) {
            Map<K, V> map = newMapInstance(2);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3) {
            Map<K, V> map = newMapInstance(3);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4) {
            Map<K, V> map = newMapInstance(4);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5) {
            Map<K, V> map = newMapInstance(5);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            safePut(map, k5, v5);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                            @NotNull K k6, @NotNull V v6) {
            Map<K, V> map = newMapInstance(6);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            safePut(map, k5, v5);
            safePut(map, k6, v6);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                            @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7) {
            Map<K, V> map = newMapInstance(7);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            safePut(map, k5, v5);
            safePut(map, k6, v6);
            safePut(map, k7, v7);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                            @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8) {
            Map<K, V> map = newMapInstance(8);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            safePut(map, k5, v5);
            safePut(map, k6, v6);
            safePut(map, k7, v7);
            safePut(map, k8, v8);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                            @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8, @NotNull K k9, @NotNull V v9) {
            Map<K, V> map = newMapInstance(9);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            safePut(map, k5, v5);
            safePut(map, k6, v6);
            safePut(map, k7, v7);
            safePut(map, k8, v8);
            safePut(map, k9, v9);
            return finish(map);
        }

        public <K, V> Map<K, V> of(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5,
                            @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8, @NotNull K k9, @NotNull V v9, @NotNull K k10, @NotNull V v10) {
            Map<K, V> map = newMapInstance(10);
            safePut(map, k1, v1);
            safePut(map, k2, v2);
            safePut(map, k3, v3);
            safePut(map, k4, v4);
            safePut(map, k5, v5);
            safePut(map, k6, v6);
            safePut(map, k7, v7);
            safePut(map, k8, v8);
            safePut(map, k9, v9);
            safePut(map, k10, v10);
            return finish(map);
        }

        @SuppressWarnings("unchecked")
        private <K, V> @NotNull Map<K, V> newMapInstance(int expectedSize) {
            Map<K, V> map = (Map<K, V>) create.apply(expectedSize);
            if (map == null) {
                throw new CreateInstanceFailedException(
                        "create returned null for expectedSize=" + expectedSize);
            }
            return map;
        }

        private <K, V> Map<K, V> finish(Map<K, V> map){
            return immutable ? Collections.unmodifiableMap(map) : map;
        }
    }

    private static <K, V> void safePut(@NotNull Map<K, V> map, @NotNull K key, @NotNull V value){
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        if (map.containsKey(key)){
            throw new IllegalArgumentException("Duplicate key '" + key + "'");
        }
        map.put(key, value);
    }
}
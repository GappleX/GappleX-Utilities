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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MapModifier<K, V> {
    private final Map<K, V> targetMap;

    public MapModifier<K, V> putIfNotNull(K key, V value) {
        if (key != null && value != null) {
            targetMap.put(key, value);
        }
        return this;
    }

    public MapModifier<K, V> putAll(Map<? extends K, ? extends V> map) {
        targetMap.putAll(Objects.requireNonNull(map, "The MapModifier's arg was 'null'"));
        return this;
    }

    public int size() {
        return targetMap.size();
    }

    public MapModifier(Map<K, V> map) {
        targetMap = Objects.requireNonNull(map, "The MapModifier's arg was 'null'");
    }

    public MapModifier(){
        targetMap = new LinkedHashMap<>();
    }

    public MapModifier<K, V> put(K key, V value){
        targetMap.put(key, value);
        return this;
    }

    public MapModifier<K, V> clear(){
        targetMap.clear();
        return this;
    }

    public Map<K, V> toMap(){
        return new LinkedHashMap<>(targetMap);
    }

    public Map<K, V> toImmutableMap() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(targetMap));
    }

    Map<K, V> getRawMap(){
        return targetMap;
    }
}

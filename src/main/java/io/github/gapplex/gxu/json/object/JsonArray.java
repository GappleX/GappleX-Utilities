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
package io.github.gapplex.gxu.json.object;

import io.github.gapplex.gxu.convert.ConvertException;
import io.github.gapplex.gxu.convert.ConvertMethod;
import io.github.gapplex.gxu.json.JsonException;
import io.github.gapplex.gxu.json.JsonUtils;
import io.github.gapplex.gxu.json.parse.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Deprecated(since = "0.8.0")
public final class JsonArray extends Json implements Iterable<Object> {
    private final List<Object> list;

    public JsonArray() {
        this.list = new ArrayList<>();
    }

    public JsonArray(JsonArray array) {
        this.list = new ArrayList<>(array.list);
    }

    public JsonArray add(Object value) {
        list.add(value == null ? Json.NULL : value);
        return this;
    }

    public JsonArray remove(int index) {
        list.remove(index);
        return this;
    }

    public JsonArray remove(Object value) {
        list.remove(value == null ? Json.NULL : value);
        return this;
    }

    public JsonArray clear() {
        list.clear();
        return this;
    }

    public Object get(int index) {
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    // Primitive

    public String getString(int index)  { return asString(get(index), ctx(index)); }
    public boolean getBoolean(int index){ return asBoolean(get(index), ctx(index)); }
    public int getInt(int index)        { return asInt(get(index), ctx(index)); }
    public long getLong(int index)      { return asLong(get(index), ctx(index)); }
    public short getShort(int index)    { return asShort(get(index), ctx(index)); }
    public byte getByte(int index)      { return asByte(get(index), ctx(index)); }
    public char getChar(int index)      { return asChar(get(index), ctx(index)); }
    public float getFloat(int index)    { return asFloat(get(index), ctx(index)); }
    public double getDouble(int index)  { return asDouble(get(index), ctx(index)); }

    public BigInteger getBigInteger(int index) { return asBigInteger(get(index), ctx(index)); }
    public BigDecimal getBigDecimal(int index) { return asBigDecimal(get(index), ctx(index)); }

    // Container

    public JsonObject getJsonObject(int index) { return asObject(get(index), ctx(index)); }
    public JsonArray  getJsonArray(int index)  { return asArray(get(index), ctx(index)); }

    // Collection

    public <T> List<T> getList(int index, Class<T> clazz) {
        return asList(get(index), clazz, ctx(index));
    }

    public <T> Set<T> getSet(int index, Class<T> clazz) {
        return asSet(get(index), clazz, ctx(index));
    }

    public <K, V> Map<K, V> getMap(int index, Class<K> kt, Class<V> vt) {
        return asMap(get(index), kt, vt, ctx(index));
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public String toString() {
        return JsonUtils.objectToJson(list);
    }

    public String toStringWithStack(Set<Object> stack) {
        return JsonUtils.objectToJson(list, stack);
    }

    @Override
    public @NotNull Iterator<Object> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    private static String ctx(int index) {
        return "Index " + index;
    }

    public JsonArray addAll(Collection<?> json){
        json.forEach(this::add);
        return this;
    }

    public JsonArray addAll(JsonArray json){
        json.forEach(this::add);
        return this;
    }

    public static JsonArray valueOf(String json){
        return valueOf(new JsonParser(json).parseArray());
    }

    public static JsonArray valueOf(Collection<?> json){
        return new JsonArray().addAll(json);
    }

    @ConvertMethod
    private static JsonArray parseArray(Object o) {
        if (o instanceof Collection) {
            return valueOf((Collection<?>) o);
        }
        String json = (o instanceof String) ? (String) o : o.toString();
        try {
            return valueOf(json);
        } catch (JsonException e) {
            throw new ConvertException(
                    "Cannot convert " + o.getClass().getName() + " to JsonArray: " + json, e);
        }
    }

    public List<Object> toList(){
        return new ArrayList<>(list);
    }
}
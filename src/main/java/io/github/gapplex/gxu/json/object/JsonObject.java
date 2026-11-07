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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Deprecated(since = "0.8.0")
public final class JsonObject extends Json {
    private final Map<String, Object> map;

    public JsonObject(){
        map = new LinkedHashMap<>();
    }

    public JsonObject(JsonObject object){
        this.map = new LinkedHashMap<>(object.map);
    }

    public Object get(String key){
        if (!map.containsKey(key)){
            return null;
        }
        return map.get(key);
    }

    public JsonObject put(String key, Object value){
        map.put(key, value == null ? Json.NULL : value);
        return this;
    }

    public JsonObject remove(String key) {
        map.remove(key);
        return this;
    }

    public JsonObject clear(){
        map.clear();
        return this;
    }

    // Primitive

    public String getString(String key)  { return asString(get(key), ctx(key)); }
    public boolean getBoolean(String key){ return asBoolean(get(key), ctx(key)); }
    public int getInt(String key)        { return asInt(get(key), ctx(key)); }
    public long getLong(String key)      { return asLong(get(key), ctx(key)); }
    public short getShort(String key)    { return asShort(get(key), ctx(key)); }
    public byte getByte(String key)      { return asByte(get(key), ctx(key)); }
    public char getChar(String key)      { return asChar(get(key), ctx(key)); }
    public float getFloat(String key)    { return asFloat(get(key), ctx(key)); }
    public double getDouble(String key)  { return asDouble(get(key), ctx(key)); }
    public BigInteger getBigInteger(String key) { return asBigInteger(get(key), ctx(key)); }
    public BigDecimal getBigDecimal(String key) { return asBigDecimal(get(key), ctx(key)); }

    // Container

    public JsonObject getJsonObject(String key) { return asObject(get(key), ctx(key)); }
    public JsonArray getJsonArray(String key)   { return asArray(get(key), ctx(key)); }

    // Collection

    public <T> List<T> getList(String key, Class<T> clazz) { return asList(get(key), clazz, ctx(key)); }
    public <T> Set<T> getSet(String key, Class<T> clazz) { return asSet(get(key), clazz, ctx(key)); }
    public <K, V> Map<K, V> getMap(String key, Class<K> kt, Class<V> vt) { return asMap(get(key), kt, vt, ctx(key)); }

    private static String ctx(String key) { return "Key '" + key + "'"; }

    public int size(){
        return map.size();
    }

    public String toString(){
        return JsonUtils.objectToJson(map);
    }

    public String toStringWithStack(Set<Object> stack){
        return JsonUtils.objectToJson(map, stack);
    }

    public Set<String> keySet(){
        return Collections.unmodifiableSet(map.keySet());
    }

    public Collection<Object> values(){
        return Collections.unmodifiableCollection(map.values());
    }

    public Set<Map.Entry<String, Object>> entrySet(){
        return Collections.unmodifiableSet(map.entrySet());
    }

    public JsonObject putAll(JsonObject object){
        for (Map.Entry<String, ?> entry : object.entrySet()){
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public JsonObject putAll(Map<String, ?> object){
        for (Map.Entry<String, ?> entry : object.entrySet()){
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }

    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public static JsonObject valueOf(String json){
        return valueOf(new JsonParser(json).parseObject());
    }

    public static JsonObject valueOf(Map<String, ?> json){
        return new JsonObject().putAll(json);
    }

    @ConvertMethod
    private static JsonObject parseObject(Object o) {
        if (o instanceof Map) {
            JsonObject result = new JsonObject();
            ((Map<?, ?>) o).forEach((k, v) -> {
                if (!(k instanceof String)) {
                    throw new ConvertException("Map key '" + k + "' is not a String: " + o.getClass().getName());
                } else {
                    result.put((String) k, v);
                }
            });
            return result;
        }
        String json = (o instanceof String) ? (String) o : o.toString();
        try {
            return valueOf(json);
        } catch (JsonException e) {
            throw new ConvertException(
                    "Cannot convert " + o.getClass().getName() + " to JsonObject: " + json, e);
        }
    }

    public LinkedHashMap<String, Object> toMap(){
        return new LinkedHashMap<>(map);
    }
}

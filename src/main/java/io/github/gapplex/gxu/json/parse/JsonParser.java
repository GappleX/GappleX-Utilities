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
package io.github.gapplex.gxu.json.parse;

import io.github.gapplex.gxu.json.JsonException;

import java.util.*;
import java.util.function.Supplier;

public class JsonParser {
    private final JsonLexer lexer;

    public JsonParser(String json) {
        this.lexer = new JsonLexer(Objects.requireNonNull(json, "json"));
    }

    /**
     * 解析任意 JSON 值（对象、数组、基础类型）。
     * 对象将解析为 Map<String, Object>，数组解析为 List<Object>。
     */
    public Object parse() {
        return parseComplete(this::parseValue);
    }

    /**
     * 严格解析 JSON 对象。
     * @return 原生 LinkedHashMap，保持字段顺序
     */
    public Map<String, Object> parseObject() {
        return parseComplete(this::readObject);
    }

    /**
     * 严格解析 JSON 数组。
     * @return 原生 ArrayList
     */
    public List<Object> parseArray() {
        return parseComplete(this::readArray);
    }

    private <T> T parseComplete(Supplier<T> parser) {
        T value = parser.get();
        JsonToken leftover = lexer.peek();
        if (leftover.getType() != JsonTokenType.EOF) {
            throw new JsonException(
                    "Unexpected trailing token " + leftover.getType()
                            + " at " + leftover.getPosition());
        }
        return value;
    }

    private Map<String, Object> readObject() {
        consume(JsonTokenType.OBJECT_START);
        Map<String, Object> obj = new LinkedHashMap<>();

        if (lexer.peek().getType() == JsonTokenType.OBJECT_END) {
            lexer.next();
            return obj;
        }

        while (true) {
            JsonToken key = consume(JsonTokenType.STRING);
            consume(JsonTokenType.COLON);
            Object value = parseValue();
            obj.put((String) key.getValue(), value);

            JsonToken sep = lexer.next();
            if (sep.getType() == JsonTokenType.OBJECT_END) {
                return obj;
            }
            if (sep.getType() != JsonTokenType.COMMA) {
                throw new JsonException("Expected ',' or '}' at " + sep.getPosition());
            }
        }
    }

    private List<Object> readArray() {
        consume(JsonTokenType.ARRAY_START);
        List<Object> arr = new ArrayList<>();

        if (lexer.peek().getType() == JsonTokenType.ARRAY_END) {
            lexer.next();
            return arr;
        }

        while (true) {
            arr.add(parseValue());

            JsonToken sep = lexer.next();
            if (sep.getType() == JsonTokenType.ARRAY_END) {
                return arr;
            }
            if (sep.getType() != JsonTokenType.COMMA) {
                throw new JsonException("Expected ',' or ']' at " + sep.getPosition());
            }
        }
    }

    private Object parseValue() {
        JsonToken t = lexer.peek();
        switch (t.getType()) {
            case OBJECT_START: return readObject();
            case ARRAY_START:  return readArray();

            case STRING:
            case NUMBER:
            case BOOLEAN:
                lexer.next();
                return t.getValue();

            case NULL:
                lexer.next();
                return null;

            default:
                throw new JsonException("Unexpected token " + t.getType() + " at " + t.getPosition());
        }
    }

    private JsonToken consume(JsonTokenType expected) {
        JsonToken t = lexer.next();
        if (t.getType() != expected) {
            throw new JsonException(
                    "Expected " + expected + " but got " + t.getType() + " at " + t.getPosition());
        }
        return t;
    }
}
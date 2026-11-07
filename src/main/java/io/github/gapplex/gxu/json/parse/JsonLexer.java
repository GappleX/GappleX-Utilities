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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public final class JsonLexer {
    private static final String NUMBER_CHARS = "-+0123456789.eE";

    private JsonToken currentToken;
    private final String json;
    private int pos;

    public JsonLexer(String json){
        this.json = Objects.requireNonNull(json, "json");
        pos = 0;
        currentToken = nextToken();
    }

    public JsonToken peek(){
        return currentToken;
    }

    public boolean hasNext(){
        return currentToken.getType() != JsonTokenType.EOF;
    }

    public JsonToken next(){
        JsonToken t = currentToken;
        currentToken = nextToken();
        return t;
    }

    private JsonToken nextToken(){
        skipWhitespace();
        if (pos >= json.length()) {
            return new JsonToken(JsonTokenType.EOF, null, pos);
        }
        char currentChar = json.charAt(pos);
        switch (currentChar){
            //Single
            case '{': return singleChar(JsonTokenType.OBJECT_START);
            case '}': return singleChar(JsonTokenType.OBJECT_END);
            case '[': return singleChar(JsonTokenType.ARRAY_START);
            case ']': return singleChar(JsonTokenType.ARRAY_END);
            case ':': return singleChar(JsonTokenType.COLON);
            case ',': return singleChar(JsonTokenType.COMMA);
            //String
            case '"': return readString();
            //Number
            case '-': case '0':
            case '1': case '2': case '3':
            case '4': case '5': case '6':
            case '7': case '8': case '9':
                return readNumber();
            //True,False or Null
            case 'n':
            case 't':
            case 'f':
                return readLiteral();
        }
        throw new JsonException("Unexpected char '" + currentChar + "' at " + pos);
    }

    private void skipWhitespace() {
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    private JsonToken readString() {
        int start = pos;
        pos++;
        StringBuilder sb = new StringBuilder(16);
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == '"') {
                pos++;
                return new JsonToken(JsonTokenType.STRING, sb.toString(), start);
            } else if (c == '\\') {
                pos++;
                if (pos >= json.length()) {
                    throw new JsonException("Unterminated escape at " + (pos - 1));
                }
                char next = json.charAt(pos);
                switch (next) {
                    case 'n': sb.append('\n');break;
                    case 't': sb.append('\t');break;
                    case 'b': sb.append('\b');break;
                    case 'r': sb.append('\r');break;
                    case 'f': sb.append('\f');break;
                    case '\\': sb.append('\\');break;
                    case '"': sb.append('"');break;
                    case '/':  sb.append('/');  break;
                    case 'u': {
                        if (pos + 4 >= json.length()) {
                            throw new JsonException("Invalid unicode escape at " + pos);
                        }
                        int code = 0;
                        for (int i = 1; i <= 4; i++) {
                            char h = json.charAt(pos + i);
                            int d = Character.digit(h, 16);
                            if (d < 0) {
                                throw new JsonException("Invalid hex digit: " + h);
                            }
                            code = (code << 4) | d;
                        }
                        sb.append((char) code);
                        pos += 4;
                        break;
                    }
                    default:
                        throw new JsonException("Invalid escape: \\" + next);
                }
                pos++;
            } else {
                sb.append(c);
                pos++;
            }
        }
        throw new JsonException("Unterminated string starting at " + start);
    }

    private JsonToken readNumber() {
        int start = pos;
        while (pos < json.length() && NUMBER_CHARS.indexOf(json.charAt(pos)) >= 0) {
            pos++;
        }
        String raw = json.substring(start, pos);
        return new JsonToken(JsonTokenType.NUMBER, parseNumber(raw), start);
    }

    private Number parseNumber(String s){
        int dotPos = s.indexOf('.');
        if (s.length() > 1 && s.charAt(0) == '0' && dotPos == -1) {
            throw new JsonException("Leading zero not allowed: " + s);
        }
        try {
            if (s.indexOf('e') != -1 || s.indexOf('E') != -1 || dotPos != -1){
                return new BigDecimal(s);
            } else {
                return new BigInteger(s);
            }
        } catch (NumberFormatException e){
            throw new JsonException("Invalid number: " + s, e);
        }
    }

    private JsonToken readLiteral() {
        int start = pos;
        if (json.startsWith("true", pos))  { pos += 4; return new JsonToken(JsonTokenType.BOOLEAN,  Boolean.TRUE,  start); }
        if (json.startsWith("false", pos)) { pos += 5; return new JsonToken(JsonTokenType.BOOLEAN, Boolean.FALSE, start); }
        if (json.startsWith("null", pos))  { pos += 4; return new JsonToken(JsonTokenType.NULL,  null,          start); }
        throw new JsonException("Invalid literal at " + start + ": " + json.substring(start, Math.min(start + 5, json.length())));
    }

    private JsonToken singleChar(JsonTokenType type) {
        return new JsonToken(type, json.charAt(pos), pos++);
    }
}

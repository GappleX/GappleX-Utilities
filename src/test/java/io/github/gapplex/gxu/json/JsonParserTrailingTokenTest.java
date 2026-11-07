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
package io.github.gapplex.gxu.json;

import io.github.gapplex.gxu.json.parse.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTrailingTokenTest {

    /**
     * 辅助方法：由于 JsonUtils 已废弃，直接使用 JsonParser 来验证合法性。
     */
    private boolean isValid(String json) {
        try {
            new JsonParser(json).parse();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Test
    void parseAcceptsSingleValueAndTrailingWhitespace() {
        assertEquals(Boolean.TRUE, new JsonParser("true").parse());
        assertEquals(Boolean.TRUE, new JsonParser("true \n\t").parse());
        assertEquals("ok", new JsonParser("\"ok\"").parse());

        assertInstanceOf(Map.class, new JsonParser("{}").parse());
        assertInstanceOf(List.class, new JsonParser("[]").parse());

        assertTrue(isValid(" { \"a\": 1 } \n"));
    }

    @Test
    void parseRejectsTrailingTokens() {
        assertFalse(isValid("true extra"));
        assertFalse(isValid("1 2"));
        assertFalse(isValid("{}{}"));
        assertFalse(isValid("[1] [2]"));
        assertFalse(isValid("{\"a\":1} true"));

        assertThrows(JsonException.class, () -> new JsonParser("true extra").parse());
        assertThrows(JsonException.class, () -> new JsonParser("{\"a\":1} 0").parseObject());
        assertThrows(JsonException.class, () -> new JsonParser("[1] ,").parseArray());
    }
}
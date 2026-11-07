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

import io.github.gapplex.gxu.json.object.JsonArray;
import io.github.gapplex.gxu.json.object.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonParserTrailingTokenTest {

    @Test
    void parseAcceptsSingleValueAndTrailingWhitespace() {
        assertEquals(Boolean.TRUE, JsonUtils.parse("true"));
        assertEquals(Boolean.TRUE, JsonUtils.parse("true \n\t"));
        assertEquals("ok", JsonUtils.parse("\"ok\""));
        assertTrue(JsonUtils.parse("{}") instanceof JsonObject);
        assertTrue(JsonUtils.parse("[]") instanceof JsonArray);
        assertTrue(JsonUtils.isValid(" { \"a\": 1 } \n"));
    }

    @Test
    void parseRejectsTrailingTokens() {
        assertFalse(JsonUtils.isValid("true extra"));
        assertFalse(JsonUtils.isValid("1 2"));
        assertFalse(JsonUtils.isValid("{}{}"));
        assertFalse(JsonUtils.isValid("[1] [2]"));
        assertFalse(JsonUtils.isValid("{\"a\":1} true"));

        assertThrows(JsonException.class, () -> JsonUtils.parse("true extra"));
        assertThrows(JsonException.class, () -> JsonObject.valueOf("{\"a\":1} 0"));
        assertThrows(JsonException.class, () -> JsonArray.valueOf("[1] ,"));
    }
}

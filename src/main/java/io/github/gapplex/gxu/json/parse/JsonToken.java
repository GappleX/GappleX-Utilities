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

import java.util.Objects;

public class JsonToken {
    private final JsonTokenType type;
    private final Object value;
    private final int position;

    JsonToken(JsonTokenType type, Object value, int position) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = value;
        this.position = position;
    }

    JsonTokenType getType() { return type; }
    Object getValue() { return value; }
    int getPosition() { return position; }
}

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

public enum JsonTokenType {
    OBJECT_START(Type.SPECIAL),
    OBJECT_END(Type.SPECIAL),
    ARRAY_START(Type.SPECIAL),
    ARRAY_END(Type.SPECIAL),
    COLON(Type.SPECIAL),
    COMMA(Type.SPECIAL),
    STRING(Type.VALUE),
    NUMBER(Type.VALUE),
    BOOLEAN(Type.VALUE),
    NULL(Type.VALUE),
    EOF(Type.SPECIAL);

    enum Type{
        VALUE,
        SPECIAL
    }
    private final Type type;

    JsonTokenType(Type type){
        this.type = type;
    }

    public boolean isValue(){
        return type == Type.VALUE;
    }
}

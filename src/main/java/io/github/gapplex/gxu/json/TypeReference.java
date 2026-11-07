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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 用于在 Java 8 下获取泛型 {@link Type}。
 *
 * <pre>{@code
 * List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>() {});
 * }</pre>
 *
 * @param <T> 目标类型
 * @author gapplex
 */
@Deprecated(since = "0.8.0")
public abstract class TypeReference<T> {

    private final Type type;

    /**
     * 子类必须以匿名内部类方式创建，例如
     * {@code new TypeReference<List<User>>() {}}。
     */
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType)) {
            throw new JsonException(
                    "TypeReference must be created with a generic type parameter");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * @return 泛型类型
     */
    public Type getType() {
        return type;
    }
}
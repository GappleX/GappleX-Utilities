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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定字段在 JSON 中的键名，可用于字段和枚举常量。
 *
 * <pre>{@code
 * public class User {
 *     @JsonProperty("user_name")
 *     private String userName;
 * }
 *
 * public enum Status {
 *     @JsonProperty("ok")   SUCCESS,
 *     @JsonProperty("fail") FAILURE
 * }
 * }</pre>
 *
 * @author gapplex
 */
@Deprecated(since = "0.8.0")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonProperty {

    /**
     * @return JSON 键名；空字符串表示使用字段原名
     */
    String value() default "";
}
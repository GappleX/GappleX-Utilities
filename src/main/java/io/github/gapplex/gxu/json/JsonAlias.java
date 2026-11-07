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
 * 反序列化时允许使用的额外键名（别名）。
 *
 * <p>序列化输出时只使用 {@link JsonProperty} 或字段原名，别名仅用于读取。</p>
 *
 * <pre>{@code
 * public class User {
 *     @JsonProperty("userName")
 *     @JsonAlias({"user_name", "name"})
 *     private String name;
 * }
 * }</pre>
 *
 * @author gapplex
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonAlias {

    /**
     * @return 允许的别名列表
     */
    String[] value() default {};
}
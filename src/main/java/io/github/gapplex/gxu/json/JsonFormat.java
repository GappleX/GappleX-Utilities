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
 * 指定日期、时间字段的序列化与反序列化格式。
 *
 * <pre>{@code
 * public class Order {
 *     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
 *     private Date createdAt;
 *
 *     @JsonFormat(pattern = "yyyy-MM-dd")
 *     private LocalDate day;
 * }
 * }</pre>
 *
 * @author gapplex
 */
@Deprecated(since = "0.8.0")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonFormat {

    /**
     * @return 日期格式，遵循 {@code SimpleDateFormat} / {@code DateTimeFormatter} 语法；
     *         为空时使用 ISO-8601
     */
    String pattern() default "";

    /**
     * @return 时区 ID，如 {@code UTC}、{@code GMT+8}、{@code Asia/Shanghai}；
     *         为空时使用系统默认时区
     */
    String timezone() default "";
}
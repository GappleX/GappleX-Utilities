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

import io.github.gapplex.gxu.json.object.Json;
import io.github.gapplex.gxu.json.object.JsonArray;
import io.github.gapplex.gxu.json.object.JsonObject;
import io.github.gapplex.gxu.json.parse.JsonParser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 轻量级 JSON 序列化 / 反序列化工具。
 *
 * <h3>主要能力</h3>
 * <ul>
 *   <li>对象 → JSON：POJO、{@link Map}、{@link Collection}、数组、
 *       {@link JsonObject}/{@link JsonArray}、{@link Optional}、枚举</li>
 *   <li>日期时间：{@link Date}、{@link Calendar}、{@code java.time} 全家桶
 *       （{@link LocalDate}/{@link LocalDateTime}/{@link Instant}/...），
 *       默认 ISO-8601，可用 {@link JsonFormat} 覆盖</li>
 *   <li>JSON → 对象：支持泛型（{@code List<User>}、{@code Map<String, User>}）、
 *       嵌套 POJO、数组、{@link Optional}、枚举</li>
 *   <li>注解：{@link JsonIgnore}、{@link JsonProperty}、{@link JsonAlias}、{@link JsonFormat}</li>
 *   <li>循环引用检测，输出 {@code "<circular>"} 占位</li>
 *   <li>非有限浮点数（NaN/Infinity）序列化为 {@code null}</li>
 *   <li>解析失败时抛出 {@link JsonException}，语义与 {@code JsonParser} 保持一致</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>所有方法均为无状态静态方法，可安全并发调用。</p>
 *
 * <h3>示例</h3>
 * <pre>{@code
 * String json  = JsonUtils.toJson(user);
 * User   user  = JsonUtils.fromJson(json, User.class);
 * List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>() {});
 * }</pre>
 *
 * @author gapplex
 */
@Deprecated(since = "0.8.0")
public final class JsonUtils {

    private static final char[] HEX_TABLE = "0123456789abcdef".toCharArray();

    private JsonUtils() {
    }

    /**
     * 把任意对象序列化为 JSON 文本。
     *
     * @param value 待序列化对象，允许为 {@code null}
     * @return JSON 文本
     */
    public static String toJson(Object value) {
        return toJson(value, null);
    }

    /**
     * 把任意对象序列化为 JSON 文本，并指定其静态类型。
     *
     * @param value 待序列化对象，允许为 {@code null}
     * @param type  静态类型，可为 {@code null}
     * @return JSON 文本
     */
    public static String toJson(Object value, Type type) {
        Set<Object> stack = Collections.newSetFromMap(
                new IdentityHashMap<>());
        return objectToJson(value, type, stack);
    }

    /**
     * {@link #toJson(Object)} 的旧命名别名。
     *
     * @param value 待序列化对象
     * @return JSON 文本
     */
    public static String objectToJson(Object value) {
        return toJson(value);
    }

    /**
     * {@link #toJson(Object, Type)} 的旧命名别名。
     *
     * @param value 待序列化对象
     * @param type  静态类型
     * @return JSON 文本
     */
    public static String objectToJson(Object value, Type type) {
        return toJson(value, type);
    }

    /**
     * 供 {@link JsonObject#toStringWithStack(Set)} 与
     * {@link JsonArray#toStringWithStack(Set)} 调用的入口。
     *
     * @param value 待序列化对象
     * @param stack 循环引用检测栈
     * @return JSON 文本
     */
    public static String objectToJson(Object value, Set<Object> stack) {
        return objectToJson(value, null, null, stack);
    }

    /**
     * 携带类型信息、不携带字段元信息的序列化入口。
     *
     * @param value 待序列化对象
     * @param type  静态类型，可为 {@code null}
     * @param stack 循环引用检测栈
     * @return JSON 文本
     */
    public static String objectToJson(Object value, Type type, Set<Object> stack) {
        return objectToJson(value, type, null, stack);
    }

    /**
     * 核心序列化实现。
     *
     * @param value   待序列化对象
     * @param type    静态类型，可为 {@code null}
     * @param element 字段元信息，用于读取 {@link JsonFormat} 等注解，可为 {@code null}
     * @param stack   循环引用检测栈
     * @return JSON 文本
     */
    public static String objectToJson(Object value, Type type,
                                      AnnotatedElement element, Set<Object> stack) {
        if (value == null || value == Json.NULL) {
            return "null";
        }

        final Class<?> valueClass = value.getClass();

        // ---------- 标量 ----------
        if (valueClass == String.class || valueClass == Character.class) {
            return quote(String.valueOf(value));
        }
        if (valueClass == Boolean.class) {
            return String.valueOf(value);
        }
        if (valueClass == Float.class) {
            float f = (Float) value;
            return Float.isFinite(f) ? String.valueOf(f) : "null";
        }
        if (valueClass == Double.class) {
            double d = (Double) value;
            return Double.isFinite(d) ? String.valueOf(d) : "null";
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof CharSequence) {
            return quote(value.toString());
        }

        // ---------- 日期时间 ----------
        if (value instanceof Date) {
            return writeDate((Date) value, element);
        }
        if (value instanceof Calendar) {
            return writeDate(((Calendar) value).getTime(), element);
        }
        if (value instanceof TemporalAccessor) {
            return writeTemporal((TemporalAccessor) value, element);
        }
        if (value instanceof Duration || value instanceof Period) {
            return quote(value.toString());
        }

        // ---------- 枚举 ----------
        if (value instanceof Enum) {
            return quote(enumName((Enum<?>) value));
        }

        // ---------- Optional ----------
        if (value instanceof Optional) {
            Optional<?> opt = (Optional<?>) value;
            if (!opt.isPresent()) {
                return "null";
            }
            return objectToJson(opt.get(), typeArgument(type, 0), element, stack);
        }

        // ---------- 容器 ----------
        if (stack.contains(value)) {
            return "\"<circular>\"";
        }
        stack.add(value);
        try {
            if (value instanceof JsonObject) {
                return ((JsonObject) value).toStringWithStack(stack);
            }
            if (value instanceof JsonArray) {
                return ((JsonArray) value).toStringWithStack(stack);
            }
            if (value instanceof Map) {
                return mapToJson((Map<?, ?>) value, type, stack);
            }
            if (value instanceof Collection) {
                return collectionToJson((Collection<?>) value, type, stack);
            }
            if (valueClass.isArray()) {
                return arrayToJson(value, type, stack);
            }
            return pojoToJson(value, type, stack);
        } finally {
            stack.remove(value);
        }
    }

    private static String mapToJson(Map<?, ?> map, Type type, Set<Object> stack) {
        Type valueType = typeArgument(type, 1);
        StringBuilder result = new StringBuilder(map.size() * 32 + 2);
        result.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (key == null) {
                continue;
            }
            if (!first) {
                result.append(',');
            }
            first = false;
            result.append('"').append(safeString(String.valueOf(key))).append("\":");
            result.append(objectToJson(entry.getValue(), valueType, stack));
        }
        result.append('}');
        return result.toString();
    }

    private static String collectionToJson(Collection<?> collection, Type type, Set<Object> stack) {
        Type elementType = typeArgument(type, 0);
        StringBuilder result = new StringBuilder(collection.size() * 24 + 2);
        result.append('[');
        boolean first = true;
        for (Object o : collection) {
            if (!first) {
                result.append(',');
            }
            first = false;
            result.append(objectToJson(o, elementType, stack));
        }
        result.append(']');
        return result.toString();
    }

    private static String arrayToJson(Object array, Type type, Set<Object> stack) {
        Type componentType = componentType(type, array.getClass());
        int length = Array.getLength(array);
        StringBuilder result = new StringBuilder(length * 16 + 2);
        result.append('[');
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                result.append(',');
            }
            result.append(objectToJson(Array.get(array, i), componentType, stack));
        }
        result.append(']');
        return result.toString();
    }

    private static String pojoToJson(Object value, Type type, Set<Object> stack) {
        Class<?> clazz = rawClass(type);
        Class<?> actual = value.getClass();
        if (clazz == null
                || clazz == Object.class
                || !clazz.isAssignableFrom(actual)
                || clazz.isInterface()
                || Modifier.isAbstract(clazz.getModifiers())) {
            clazz = actual;
        }

        StringBuilder result = new StringBuilder(64);
        result.append('{');
        boolean first = true;
        Set<String> written = new LinkedHashSet<>();

        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod)
                        || Modifier.isTransient(mod)
                        || field.isSynthetic()
                        || field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }

                JsonProperty jp = field.getAnnotation(JsonProperty.class);
                String name = (jp != null && !jp.value().isEmpty())
                        ? jp.value() : field.getName();
                if (!written.add(name)) {
                    continue;
                }

                Object fieldValue;
                try {
                    field.setAccessible(true);
                    fieldValue = field.get(value);
                } catch (Exception e) {
                    // 单字段访问失败不影响整体
                    continue;
                }

                if (!first) {
                    result.append(',');
                }
                first = false;
                result.append('"').append(safeString(name)).append("\":");
                result.append(objectToJson(fieldValue, field.getGenericType(), field, stack));
            }
        }
        result.append('}');
        return result.toString();
    }

    private static String writeDate(Date date, AnnotatedElement element) {
        if (element != null) {
            JsonFormat fmt = element.getAnnotation(JsonFormat.class);
            if (fmt != null && !fmt.pattern().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt.pattern(), Locale.US);
                if (!fmt.timezone().isEmpty()) {
                    sdf.setTimeZone(TimeZone.getTimeZone(fmt.timezone()));
                }
                return quote(sdf.format(date));
            }
        }
        return quote(DateTimeFormatter.ISO_INSTANT
                .format(Instant.ofEpochMilli(date.getTime())));
    }

    /**
     * 序列化 {@code java.time} 类型。
     *
     * <p>如果字段上存在 {@link JsonFormat} 且指定了 pattern，则按该格式输出；
     * 否则回退到该类型的 {@link Object#toString()}（即 ISO-8601）。</p>
     *
     * @param value   待序列化的时间对象
     * @param element 字段元信息，可为 {@code null}
     * @return JSON 字符串字面量
     */
    private static String writeTemporal(TemporalAccessor value, AnnotatedElement element) {
        if (element != null) {
            JsonFormat fmt = element.getAnnotation(JsonFormat.class);
            if (fmt != null && !fmt.pattern().isEmpty()) {
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern(fmt.pattern(), Locale.US);
                if (!fmt.timezone().isEmpty()) {
                    formatter = formatter.withZone(ZoneId.of(fmt.timezone()));
                }
                try {
                    if (value instanceof Instant) {
                        ZoneId zone = formatter.getZone();
                        if (zone == null) {
                            zone = ZoneId.systemDefault();
                        }
                        return quote(formatter.withZone(zone).format((Instant) value));
                    }
                    return quote(formatter.format(value));
                } catch (RuntimeException ignored) {
                    // 格式不匹配时退回 ISO-8601
                }
            }
        }
        return quote(value.toString());
    }

    /**
     * 转义 JSON 字符串中的特殊字符（不含首尾引号）。
     *
     * @param s 原始字符串
     * @return 转义后的字符串
     */
    public static String safeString(String s) {
        StringBuilder result = new StringBuilder(s.length() + 64);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                default:
                    if (c < 32) {
                        result.append("\\u00")
                                .append(HEX_TABLE[(c >> 4) & 0xF])
                                .append(HEX_TABLE[c & 0xF]);
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }

    /**
     * 解析 JSON 文本为 {@link JsonObject}、{@link JsonArray} 或标量。
     *
     * @param json JSON 文本
     * @return 解析结果
     * @throws JsonException JSON 非法时
     */
    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    /**
     * 判断给定文本是否为合法 JSON。
     *
     * @param json 待校验文本
     * @return {@code true} 表示合法
     */
    public static boolean isValid(String json) {
        try {
            parse(json);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * 把 JSON 文本反序列化为指定类型。
     *
     * @param json JSON 文本，允许为 {@code null}
     * @param type 目标类型，可以是 {@link Class} 或 {@link ParameterizedType}
     * @param <T>  目标类型泛型
     * @return 反序列化结果；{@code json} 为 {@code null} 时返回 {@code null}
     * @throws JsonException JSON 非法或转换失败时
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Type type) {
        if (json == null) {
            return null;
        }
        Object parsed = new JsonParser(json).parse();
        return (T) convert(parsed, type, null);
    }

    /**
     * 把 JSON 文本反序列化为指定类型。
     *
     * @param json  JSON 文本，允许为 {@code null}
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 反序列化结果；{@code json} 为 {@code null} 时返回 {@code null}
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return fromJson(json, (Type) clazz);
    }

    /**
     * 把 JSON 文本反序列化为泛型类型。
     *
     * @param json          JSON 文本
     * @param typeReference 泛型引用
     * @param <T>           目标类型泛型
     * @return 反序列化结果
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (typeReference == null) {
            throw new JsonException("TypeReference must not be null");
        }
        return fromJson(json, typeReference.getType());
    }

    /**
     * 把已解析的 JSON 结构或任意 Java 值转换为目标类型。
     *
     * @param value 源值
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 转换结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object value, Class<T> clazz) {
        if (clazz == null) {
            return (T) value;
        }
        return (T) convert(value, clazz, null);
    }

    @SuppressWarnings({"unchecked"})
    private static Object convert(Object value, Type type, AnnotatedElement element) {
        if (type == null) {
            return value;
        }

        Class<?> raw = rawClass(type);
        if (raw == null || raw == Object.class) {
            return value;
        }

        // null / Json.NULL
        if (value == null || value == Json.NULL) {
            return defaultValue(raw);
        }

        // ---------- 原生 JSON 结构 ----------
        if (raw == JsonObject.class) {
            if (value instanceof JsonObject) {
                return value;
            }
            if (value instanceof Map) {
                JsonObject obj = new JsonObject();
                obj.putAll((Map<String, ?>) value);
                return obj;
            }
            throw new JsonException("Expected JsonObject but was "
                    + value.getClass().getSimpleName());
        }
        if (raw == JsonArray.class) {
            if (value instanceof JsonArray) {
                return value;
            }
            if (value instanceof Collection) {
                return JsonArray.valueOf((Collection<?>) value);
            }
            throw new JsonException("Expected JsonArray but was "
                    + value.getClass().getSimpleName());
        }

        // ---------- 标量 ----------
        if (raw == String.class) {
            return value instanceof String ? value : String.valueOf(value);
        }
        if (raw == Character.class || raw == char.class) {
            String s = String.valueOf(value);
            return s.isEmpty() ? '\0' : s.charAt(0);
        }
        if (raw == Boolean.class || raw == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            String s = String.valueOf(value).trim();
            return "true".equalsIgnoreCase(s) || "1".equals(s);
        }
        if (raw == Integer.class || raw == int.class)     return toNumber(value).intValue();
        if (raw == Long.class    || raw == long.class)    return toNumber(value).longValue();
        if (raw == Double.class  || raw == double.class)  return toNumber(value).doubleValue();
        if (raw == Float.class   || raw == float.class)   return toNumber(value).floatValue();
        if (raw == Short.class   || raw == short.class)   return toNumber(value).shortValue();
        if (raw == Byte.class    || raw == byte.class)    return toNumber(value).byteValue();
        if (raw == BigDecimal.class)                      return toBigDecimal(value);
        if (raw == BigInteger.class)                      return toBigInteger(value);

        // ---------- 枚举 ----------
        if (raw.isEnum()) {
            return toEnum(value, raw);
        }

        // ---------- 日期时间 ----------
        if (raw == Date.class) {
            return readDate(value, element);
        }
        if (raw == java.sql.Date.class) {
            Date d = readDate(value, element);
            return d == null ? null : new java.sql.Date(d.getTime());
        }
        if (raw == java.sql.Timestamp.class) {
            Date d = readDate(value, element);
            return d == null ? null : new java.sql.Timestamp(d.getTime());
        }
        if (Calendar.class.isAssignableFrom(raw)) {
            Date d = readDate(value, element);
            if (d == null) {
                return null;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal;
        }
        if (raw == LocalDate.class)      return toLocalDate(value);
        if (raw == LocalDateTime.class)  return toLocalDateTime(value);
        if (raw == LocalTime.class)      return toLocalTime(value);
        if (raw == Instant.class)        return toInstant(value);
        if (raw == ZonedDateTime.class)  return toZonedDateTime(value);
        if (raw == OffsetDateTime.class) return toOffsetDateTime(value);
        if (raw == OffsetTime.class)     return OffsetTime.parse(textOf(value));
        if (raw == Year.class)           return Year.parse(textOf(value));
        if (raw == YearMonth.class)      return YearMonth.parse(textOf(value));
        if (raw == MonthDay.class)       return MonthDay.parse(textOf(value));
        if (raw == Duration.class)       return Duration.parse(textOf(value));
        if (raw == Period.class)         return Period.parse(textOf(value));

        // ---------- Optional ----------
        if (raw == Optional.class) {
            Type inner = typeArgument(type, 0);
            Object converted = convert(value, inner, element);
            return Optional.ofNullable(converted);
        }

        // ---------- 数组 / Collection ----------
        if (value instanceof JsonArray) {
            JsonArray arr = (JsonArray) value;
            int size = arr.size();

            if (raw.isArray()) {
                Class<?> compClass = raw.getComponentType();
                Object out = Array.newInstance(compClass, size);
                Type compType = componentType(type, raw);
                for (int i = 0; i < size; i++) {
                    Array.set(out, i, convert(arr.get(i), compType, null));
                }
                return out;
            }

            if (Collection.class.isAssignableFrom(raw)) {
                Type elemType = typeArgument(type, 0);
                Collection<Object> coll = createCollection(raw);
                for (int i = 0; i < size; i++) {
                    coll.add(convert(arr.get(i), elemType, null));
                }
                return coll;
            }
        }

        // ---------- Map ----------
        if (value instanceof JsonObject && Map.class.isAssignableFrom(raw)) {
            JsonObject obj = (JsonObject) value;
            Type keyType = typeArgument(type, 0);
            Type valType = typeArgument(type, 1);
            Map<Object, Object> target = createMap(raw);
            for (Map.Entry<String, Object> e : obj.entrySet()) {
                Object key = keyType == null
                        ? e.getKey()
                        : convert(e.getKey(), keyType, null);
                target.put(key, convert(e.getValue(), valType, null));
            }
            return target;
        }

        // ---------- POJO ----------
        if (value instanceof JsonObject) {
            return convertToPojo((JsonObject) value, raw);
        }

        return value;
    }

    private static Object convertToPojo(JsonObject json, Class<?> clazz) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object instance = ctor.newInstance();

            Set<String> usedKeys = new LinkedHashSet<>();

            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod)
                            || Modifier.isTransient(mod)
                            || field.isSynthetic()
                            || field.isAnnotationPresent(JsonIgnore.class)) {
                        continue;
                    }

                    String key = findFieldKey(json, field);
                    if (key == null || !usedKeys.add(key)) {
                        continue;
                    }

                    try {
                        field.setAccessible(true);
                        field.set(instance,
                                convert(json.get(key), field.getGenericType(), field));
                    } catch (Exception ignored) {
                        // 单字段失败不影响整体
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new JsonException("Cannot deserialize into " + clazz.getName(), e);
        }
    }

    /**
     * 在 JSON 中查找与字段匹配的键：先 {@link JsonProperty}、再字段原名、最后 {@link JsonAlias}。
     *
     * @param json  JSON 对象
     * @param field 目标字段
     * @return 匹配到的键；未找到返回 {@code null}
     */
    private static String findFieldKey(JsonObject json, Field field) {
        JsonProperty jp = field.getAnnotation(JsonProperty.class);
        if (jp != null && !jp.value().isEmpty() && json.containsKey(jp.value())) {
            return jp.value();
        }
        if (json.containsKey(field.getName())) {
            return field.getName();
        }
        JsonAlias alias = field.getAnnotation(JsonAlias.class);
        if (alias != null) {
            for (String a : alias.value()) {
                if (json.containsKey(a)) {
                    return a;
                }
            }
        }
        return null;
    }

    // ==================================================================
    // 日期时间反序列化
    // ==================================================================

    private static Date readDate(Object value, AnnotatedElement element) {
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }

        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.matches("-?\\d+")) {
            return new Date(Long.parseLong(text));
        }

        JsonFormat fmt = element == null ? null : element.getAnnotation(JsonFormat.class);
        if (fmt != null && !fmt.pattern().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt.pattern(), Locale.US);
            if (!fmt.timezone().isEmpty()) {
                sdf.setTimeZone(TimeZone.getTimeZone(fmt.timezone()));
            }
            try {
                return sdf.parse(text);
            } catch (ParseException ignored) {
                // 继续尝试 ISO
            }
        }

        try {
            return Date.from(Instant.parse(text));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(OffsetDateTime.parse(text).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(ZonedDateTime.parse(text).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(LocalDateTime.parse(text)
                    .atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(LocalDate.parse(text)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(text);
            } catch (ParseException ignored) {
            }
        }

        throw new JsonException("Cannot parse date: " + text);
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String text = textOf(value);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(text).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        throw new JsonException("Cannot parse LocalDate: " + text);
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof Number) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(((Number) value).longValue()),
                    ZoneId.systemDefault());
        }

        String text = textOf(value);
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(text), ZoneId.systemDefault());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(text).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        throw new JsonException("Cannot parse LocalDateTime: " + text);
    }

    private static LocalTime toLocalTime(Object value) {
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        String text = textOf(value);
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetTime.parse(text).toLocalTime();
        } catch (DateTimeParseException ignored) {
        }
        throw new JsonException("Cannot parse LocalTime: " + text);
    }

    private static Instant toInstant(Object value) {
        if (value instanceof Instant) {
            return (Instant) value;
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }

        String text = textOf(value);
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(text).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        throw new JsonException("Cannot parse Instant: " + text);
    }

    private static ZonedDateTime toZonedDateTime(Object value) {
        if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue())
                    .atZone(ZoneId.systemDefault());
        }
        return ZonedDateTime.parse(textOf(value));
    }

    private static OffsetDateTime toOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime) {
            return (OffsetDateTime) value;
        }
        if (value instanceof Number) {
            return OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(((Number) value).longValue()),
                    ZoneId.systemDefault());
        }
        return OffsetDateTime.parse(textOf(value));
    }

    // ==================================================================
    // 枚举
    // ==================================================================

    private static String enumName(Enum<?> e) {
        try {
            Field f = e.getDeclaringClass().getField(e.name());
            JsonProperty jp = f.getAnnotation(JsonProperty.class);
            if (jp != null && !jp.value().isEmpty()) {
                return jp.value();
            }
        } catch (Exception ignored) {
        }
        return e.name();
    }

    private static Object toEnum(Object value, Class<?> raw) {
        Object[] constants = raw.getEnumConstants();
        if (value instanceof Number) {
            int ordinal = ((Number) value).intValue();
            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
            throw new JsonException("Invalid enum ordinal " + ordinal
                    + " for " + raw.getName());
        }

        String text = String.valueOf(value).trim();
        for (Object c : constants) {
            Enum<?> e = (Enum<?>) c;
            if (e.name().equals(text)) {
                return e;
            }
        }
        for (Object c : constants) {
            Enum<?> e = (Enum<?>) c;
            try {
                Field f = e.getDeclaringClass().getField(e.name());
                JsonProperty jp = f.getAnnotation(JsonProperty.class);
                if (jp != null && jp.value().equals(text)) {
                    return e;
                }
            } catch (Exception ignored) {
            }
        }
        for (Object c : constants) {
            Enum<?> e = (Enum<?>) c;
            if (e.name().equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new JsonException("Unknown enum value '" + text
                + "' for " + raw.getName());
    }

    // ==================================================================
    // 通用辅助
    // ==================================================================

    private static String quote(String s) {
        return "\"" + safeString(s) + "\"";
    }

    private static Number toNumber(Object v) {
        if (v instanceof Number) {
            return (Number) v;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1 : 0;
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            throw new JsonException("Cannot convert empty string to number");
        }
        try {
            if (s.indexOf('.') >= 0 || s.indexOf('e') >= 0 || s.indexOf('E') >= 0) {
                return Double.valueOf(s);
            }
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e2) {
                throw new JsonException("Cannot convert to number: " + s, e2);
            }
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new JsonException("Cannot convert to BigDecimal: " + value, e);
        }
    }

    private static BigInteger toBigInteger(Object value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        try {
            return new BigInteger(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new JsonException("Cannot convert to BigInteger: " + value, e);
        }
    }

    private static String textOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Object defaultValue(Class<?> raw) {
        if (raw == boolean.class) return false;
        if (raw == char.class)    return '\0';
        if (raw == byte.class)    return (byte) 0;
        if (raw == short.class)   return (short) 0;
        if (raw == int.class)     return 0;
        if (raw == long.class)    return 0L;
        if (raw == float.class)   return 0f;
        if (raw == double.class)  return 0d;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> createCollection(Class<?> raw) {
        if (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) {
            if (SortedSet.class.isAssignableFrom(raw)
                    || NavigableSet.class.isAssignableFrom(raw)) {
                return new TreeSet<>();
            }
            if (Set.class.isAssignableFrom(raw)) {
                return new LinkedHashSet<>();
            }
            if (Queue.class.isAssignableFrom(raw)) {
                return new ArrayDeque<>();
            }
            return new ArrayList<>();
        }
        try {
            return (Collection<Object>) raw.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> createMap(Class<?> raw) {
        if (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) {
            if (SortedMap.class.isAssignableFrom(raw)
                    || NavigableMap.class.isAssignableFrom(raw)) {
                return new TreeMap<>();
            }
            return new LinkedHashMap<>();
        }
        try {
            return (Map<Object, Object>) raw.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private static Class<?> rawClass(Type type) {
        if (type == null) return null;
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) {
            return rawClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof GenericArrayType) {
            Class<?> comp = rawClass(((GenericArrayType) type).getGenericComponentType());
            return comp == null ? null : Array.newInstance(comp, 0).getClass();
        }
        if (type instanceof WildcardType) {
            Type[] upper = ((WildcardType) type).getUpperBounds();
            return upper.length > 0 ? rawClass(upper[0]) : null;
        }
        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return bounds.length > 0 ? rawClass(bounds[0]) : null;
        }
        return null;
    }

    private static Type typeArgument(Type type, int index) {
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            if (index < args.length) {
                return args[index];
            }
        }
        return null;
    }

    private static Type componentType(Type type, Class<?> arrayClass) {
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        }
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            if (c.isArray()) {
                return c.getComponentType();
            }
        }
        if (type instanceof ParameterizedType) {
            Class<?> raw = rawClass(((ParameterizedType) type).getRawType());
            if (raw != null && raw.isArray()) {
                return raw.getComponentType();
            }
        }
        return arrayClass.getComponentType();
    }
}
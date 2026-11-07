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
package io.github.gapplex.gxu.unsafe;

import io.github.gapplex.gxu.convert.ConvertUtils;
import org.jetbrains.annotations.NotNull;

/**
 * {@code unsafe} 模块的统一异常。
 *
 * <p>{@link #getPath()} 返回字段路径，反序列化场景下可能是嵌套路径
 * （如 {@code Foo.bar[2].baz}），实例/静态字段场景下是 {@code Owner.field}。</p>
 */
public class UnsafeException extends RuntimeException {

    public enum Reason {
        /** 字段名在目标类及其父类中不存在。 */
        FIELD_NOT_FOUND,
        /** 实例 API 收到了静态字段。 */
        STATIC_FIELD_NOT_SUPPORTED,
        /** 静态 API 收到了实例字段。 */
        INSTANCE_FIELD_NOT_SUPPORTED,
        /** 字段声明类型与值类型不兼容。 */
        TYPE_MISMATCH,
        /** 基本类型字段收到 {@code null}。 */
        NULL_FOR_PRIMITIVE,
        /** 被注解为NotNull字段收到 {@code null}。 */
        NULL_FOR_NOTNULL,
        /** 实例字段的 base 不是声明类的实例。 */
        BASE_TYPE_MISMATCH,
        /** 构造器不可用 / 访问被拒。 */
        ACCESS_DENIED,
        /** 反序列化嵌套层数超限。 */
        NESTING_TOO_DEEP,
        /** 不支持的集合类型。 */
        UNSUPPORTED_COLLECTION,
        /** 不支持的 {@link java.lang.reflect.Type}。 */
        UNSUPPORTED_TYPE,
        /** {@link ConvertUtils} 转换失败。 */
        CONVERSION_FAILED
    }

    private final Reason reason;
    private final String path;

    public UnsafeException(Reason reason, String path, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.path = path;
    }

    public Reason getReason() { return reason; }
    public String getPath()   { return path; }

    public static UnsafeException fieldNotFound(String className, String fieldName) {
        return of(Reason.FIELD_NOT_FOUND, className + "." + fieldName,
                "Field not found: " + className + "." + fieldName);
    }

    public static UnsafeException staticFieldNotSupported(String className, String fieldName) {
        return of(Reason.STATIC_FIELD_NOT_SUPPORTED, className + "." + fieldName,
                "Static field not supported here: " + className + "." + fieldName
                        + "; use getStatic/putStatic instead");
    }

    public static UnsafeException instanceFieldNotSupported(String className, String fieldName) {
        return of(Reason.INSTANCE_FIELD_NOT_SUPPORTED, className + "." + fieldName,
                "Instance field not supported here: " + className + "." + fieldName
                        + "; use get/put instead");
    }

    public static UnsafeException typeMismatch(String path, Class<?> expected, Class<?> actual) {
        return of(Reason.TYPE_MISMATCH, path,
                "Type mismatch at " + path + ": expected " + expected.getName()
                        + ", actual " + (actual == null ? "null" : actual.getName()));
    }

    public static UnsafeException nullForPrimitive(String className, String fieldName) {
        return of(Reason.NULL_FOR_PRIMITIVE, className + "." + fieldName,
                "Cannot set primitive field to null: " + className + "." + fieldName);
    }

    public static UnsafeException baseTypeMismatch(String path, Class<?> expected, Class<?> actual) {
        return of(Reason.BASE_TYPE_MISMATCH, path,
                "Base type mismatch at " + path + ": expected instance of "
                        + expected.getName() + ", actual " + actual.getName());
    }

    public static UnsafeException accessDenied(String target, String message, Throwable cause) {
        return new UnsafeException(Reason.ACCESS_DENIED, target, message, cause);
    }

    public static UnsafeException nestingTooDeep(String path, int maxDepth) {
        return of(Reason.NESTING_TOO_DEEP, path,
                "Nesting too deep at " + path + " (max " + maxDepth + ")");
    }

    public static UnsafeException unsupportedCollection(String path, Class<?> type) {
        return of(Reason.UNSUPPORTED_COLLECTION, path,
                "Unsupported collection type at " + path + ": " + type.getName());
    }

    public static UnsafeException unsupportedType(String path, String typeName) {
        return of(Reason.UNSUPPORTED_TYPE, path,
                "Unsupported type at " + path + ": " + typeName);
    }

    public static UnsafeException conversionFailed(String path, Throwable cause) {
        return new UnsafeException(Reason.CONVERSION_FAILED, path,
                "Conversion failed at " + path + ": " + cause.getMessage(), cause);
    }

    private static UnsafeException of(Reason reason, String path, String message) {
        return new UnsafeException(reason, path, message, null);
    }
}
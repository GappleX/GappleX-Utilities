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
package io.github.gapplex.gxu.reflect;

public class ReflectException extends RuntimeException {

    public enum Reason {
        FIELD_NOT_FOUND,
        STATIC_FIELD_NOT_SUPPORTED,
        INSTANCE_FIELD_NOT_SUPPORTED,
        TYPE_MISMATCH,
        NULL_FOR_PRIMITIVE,
        ACCESS_DENIED
    }

    private final Reason reason;
    private final String fieldPath;

    public ReflectException(Reason reason, String fieldPath, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.fieldPath = fieldPath;
    }

    public Reason getReason() { return reason; }
    public String getFieldPath() { return fieldPath; }

    public static ReflectException fieldNotFound(String className, String fieldName) {
        return of(Reason.FIELD_NOT_FOUND, className, fieldName,
                "Field not found: " + className + "." + fieldName);
    }

    public static ReflectException staticFieldNotSupported(String className, String fieldName) {
        return of(Reason.STATIC_FIELD_NOT_SUPPORTED, className, fieldName,
                "Static field not supported here: " + className + "." + fieldName
                        + "; use getStaticField/setStaticField");
    }

    public static ReflectException instanceFieldNotSupported(String className, String fieldName) {
        return of(Reason.INSTANCE_FIELD_NOT_SUPPORTED, className, fieldName,
                "Instance field not supported here: " + className + "." + fieldName
                        + "; use getField/setField");
    }

    public static ReflectException typeMismatch(String className, String fieldName,
                                                Class<?> expected, Class<?> actual) {
        return of(Reason.TYPE_MISMATCH, className, fieldName,
                "Type mismatch for " + className + "." + fieldName
                        + ": expected " + expected.getName() + ", actual " + actual.getName());
    }

    public static ReflectException nullForPrimitive(String className, String fieldName) {
        return of(Reason.NULL_FOR_PRIMITIVE, className, fieldName,
                "Cannot set primitive field to null: " + className + "." + fieldName);
    }

    public static ReflectException accessDenied(String className, String fieldName, Throwable cause) {
        return new ReflectException(Reason.ACCESS_DENIED,
                className + "." + fieldName,
                "Cannot access field " + className + "." + fieldName,
                cause);
    }

    private static ReflectException of(Reason reason, String className, String fieldName, String message) {
        return new ReflectException(reason, className + "." + fieldName, message, null);
    }
}
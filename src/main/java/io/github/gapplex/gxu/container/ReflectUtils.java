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
package io.github.gapplex.gxu.container;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.IntFunction;

public class ReflectUtils<T> {
    private final CtorClassValue<T> ctors;
    public ReflectUtils(CtorClassValue<T> ctors){
        this.ctors = ctors;
    }

    public IntFunction<T> createReflectFunc(Class<? extends T> clazz){
        Objects.requireNonNull(clazz, "clazz");

        ClassRecord<T> classRecord;
        try {
            classRecord = ctors.get(clazz);
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof RuntimeException ) {
                throw (RuntimeException) cause;
            }
            throw new GetConstructorFailedException(
                    "Get '" + clazz.getName() + "' constructor failed", cause);
        }
        return expectedSize -> {
            try {
                if (classRecord.canSetSize()){
                    return classRecord.getCtor().newInstance(expectedSize);
                } else {
                    return classRecord.getCtor().newInstance();
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                String detail = cause != null ? cause.getMessage() : null;
                String message = "Create '" + clazz.getName() + "' instance failed"
                        + (detail != null ? ", cause:" + detail : "");
                throw new CreateInstanceFailedException(message, cause != null ? cause : e);
            } catch (Exception e){
                String detail = e.getMessage();
                String message = "Create '" + clazz.getName() + "' instance failed"
                        + (detail != null ? ", cause:" + detail : "");
                throw new CreateInstanceFailedException(message, e);
            }
        };
    }
}

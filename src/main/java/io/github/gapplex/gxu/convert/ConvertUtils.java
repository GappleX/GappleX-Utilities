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
package io.github.gapplex.gxu.convert;

import io.github.gapplex.gxu.math.Maths;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConvertUtils {
    private static final MethodRecord CAN_NOT_FOUND_METHOD = new MethodRecord(null, false);

    private static final ClassValue<MethodRecord> METHODS = new ClassValue<>() {
        @Override
        protected MethodRecord computeValue(@NotNull Class<?> wrappedClass) {
            for (Class<?> c = wrappedClass; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    if (method.isSynthetic()) continue;
                    if (method.isAnnotationPresent(ConvertMethod.class)) {
                        if (wrappedClass.isAssignableFrom(method.getReturnType())
                                && Modifier.isStatic(method.getModifiers())) {
                            if (method.getParameterCount() == 1) {
                                Class<?> type = method.getParameterTypes()[0];
                                if (type == String.class || type == Object.class) {
                                    method.setAccessible(true);
                                    return new MethodRecord(method, type == String.class);
                                }
                            }
                        }
                    }
                }
            }
            return CAN_NOT_FOUND_METHOD;
        }
    };

    private static final Map<Class<?>, Function<Object, ?>> USER_REGISTRY = new ConcurrentHashMap<>();

    private static class MethodRecord{
        private final Method method;
        private final boolean isString;
        public MethodRecord(Method method, boolean isString){
            this.method = method;
            this.isString = isString;
        }

        public Method getMethod(){
            return method;
        }

        public boolean isString(){
            return isString;
        }
    }

    private ConvertUtils(){
        throw new IllegalStateException();
    }

    public static <T> void register(@NotNull Class<T> clazz, @NotNull Function<Object, T> function){
        requireNonNull(clazz, "clazz");
        requireNonNull(function, "function");
        USER_REGISTRY.put(PrimitiveUtils.wrap(clazz), function);
    }

    public static <T> void unregister(@NotNull Class<T> clazz) {
        requireNonNull(clazz, "clazz");
        USER_REGISTRY.remove(PrimitiveUtils.wrap(clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(@NotNull Object o, @NotNull Class<T> clazz){
        requireNonNull(o, "o");
        requireNonNull(clazz, "clazz");
        Class<?> wrappedClass = PrimitiveUtils.wrap(clazz);
        if (wrappedClass == Void.class){
            return null;
        }
        if (wrappedClass.isInstance(o)){
            return (T) o;
        } else {
            Object result;
            Function<Object, ?> gen = USER_REGISTRY.get(wrappedClass);
            if (gen != null){
                try {
                    return (T) gen.apply(o);
                } catch (ConvertException e) {
                    throw e;
                } catch (Exception e) {
                    throw wrapThrowable(o, e);
                }
            }

            try {
                if (wrappedClass == Integer.class)      {
                    if (o instanceof Number){
                        result = Math.toIntExact(Maths.toLongExact((Number) o));
                    } else {
                        result = Integer.valueOf(o.toString());
                    }
                }
                else if (wrappedClass == Long.class)    {
                    if (o instanceof Number){
                        result = Maths.toLongExact((Number) o);
                    } else {
                        result = Long.valueOf(o.toString());
                    }
                }
                else if (wrappedClass == Double.class)  {
                    if (o instanceof Number){
                        result = ((Number) o).doubleValue();
                    } else {
                        result = Double.valueOf(o.toString());
                    }
                }
                else if (wrappedClass == Float.class)   {
                    if (o instanceof Number){
                        result = ((Number) o).floatValue();
                    } else {
                        result = Float.valueOf(o.toString());
                    }
                }
                else if (wrappedClass == Short.class)   {
                    if (o instanceof Number){
                        result = Maths.toShortExact(Maths.toLongExact((Number) o));
                    } else {
                        result = Short.valueOf(o.toString());
                    }
                }
                else if (wrappedClass == Byte.class)   {
                    if (o instanceof Number){
                        result = Maths.toByteExact(Maths.toLongExact((Number) o));
                    } else {
                        result = Byte.valueOf(o.toString());
                    }
                }
                else if (wrappedClass == Boolean.class) result = Boolean.valueOf(o.toString());
                else if (wrappedClass == Character.class) {
                    String s = o.toString();
                    if (s.length() != 1) throw new ConvertException("Char: " + s);
                    result = s.charAt(0);
                }
                else if (wrappedClass == String.class)  return (T) String.valueOf(o);
                else if (wrappedClass.isEnum()) {
                    String s = o.toString();
                    Object found = null;
                    Object[] constants = wrappedClass.getEnumConstants();
                    for (Object constant : constants) {
                        if (((Enum<?>) constant).name().equals(s)) {
                            found = constant;
                            break;
                        }
                    }
                    if (found == null) {
                        throw new ConvertException("No enum constant " + wrappedClass.getName() + "." + s);
                    }
                    result = found;
                }
                else {
                    MethodRecord methodRecord = METHODS.get(wrappedClass);
                    if (methodRecord != CAN_NOT_FOUND_METHOD){
                        try {
                            result = methodRecord.getMethod().invoke(null, methodRecord.isString ? o.toString() : o);
                        } catch (InvocationTargetException e){
                            Throwable cause = e.getCause();
                            if (cause instanceof RuntimeException){
                                if (cause instanceof ConvertException){
                                    throw (ConvertException) cause;
                                }
                                throw wrapThrowable(o, cause);
                            }
                            if (cause instanceof Error){
                                throw (Error) cause;
                            }
                            throw wrapThrowable(o, cause);
                        } catch (Exception e){
                            throw wrapThrowable(o, e);
                        }
                    } else {
                        throw new ConvertException(
                                "No @ConvertMethod found in " + wrappedClass.getName()
                                        + " for input " + o.getClass().getName());
                    }
                }
            } catch (NumberFormatException | ArithmeticException e) {
                throw wrapThrowable(o, e);
            }

            return (T) result;
        }
    }

    private static void requireNonNull(Object value, String msg){
        if (value == null){
            throw new ConvertException(msg);
        }
    }

    private static ConvertException wrapThrowable(Object source, Throwable t){
        return new ConvertException("Can not convert '" + source + "'", t);
    }
}

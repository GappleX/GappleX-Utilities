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
package io.github.gapplex.gxu.json.object;

import io.github.gapplex.gxu.convert.PrimitiveUtils;
import io.github.gapplex.gxu.json.JsonException;
import io.github.gapplex.gxu.math.Maths;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Deprecated(since = "0.8.0")
public class Json {
    public static final Object NULL = new Object() {
        @Override public int hashCode() { return 0; }
        @Override public String toString() { return "NULL_FOR_JSON"; }
    };

    private static void checkNotNullish(Object o, String context) {
        if (o == null) {
            throw new JsonException(context + " not found");
        }
        if (o == NULL) {
            throw new JsonException(context + " is JSON null");
        }
    }

    protected static int asInt(Object o, String context) {
        checkNotNullish(o, context);
        try {
            if (o instanceof Number) {
                return Math.toIntExact(Maths.toLongExact((Number) o));
            }
            if (o instanceof String) {
                return Integer.parseInt((String) o);
            }
            throw new JsonException(context + " is not a valid int: " + o);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new JsonException(context + " is not a valid int: " + o, e);
        }
    }

    protected static long asLong(Object o, String context) {
        checkNotNullish(o, context);
        try {
            if (o instanceof Number) {
                return Maths.toLongExact((Number) o);
            }
            if (o instanceof String) {
                return Long.parseLong((String) o);
            }
            throw new JsonException(context + " is not a valid long: " + o);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new JsonException(context + " is not a valid long: " + o, e);
        }
    }

    protected static short asShort(Object o, String context) {
        checkNotNullish(o, context);
        try {
            if (o instanceof Number) {
                return Maths.toShortExact(Maths.toLongExact((Number) o));
            }
            if (o instanceof String) {
                return Short.parseShort((String) o);
            }
            throw new JsonException(context + " is not a valid short: " + o);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new JsonException(context + " is not a valid short: " + o, e);
        }
    }

    protected static byte asByte(Object o, String context) {
        checkNotNullish(o, context);
        try {
            if (o instanceof Number) {
                return Maths.toByteExact(Maths.toLongExact((Number) o));
            }
            if (o instanceof String) {
                return Byte.parseByte((String) o);
            }
            throw new JsonException(context + " is not a valid byte: " + o);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new JsonException(context + " is not a valid byte: " + o, e);
        }
    }

    protected static float asFloat(Object o, String context) {
        checkNotNullish(o, context);
        float f;
        try {
            if (o instanceof Number) {
                double d = ((Number) o).doubleValue();
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    throw new ArithmeticException("not a finite number");
                }
                f = (float) d;
            } else if (o instanceof String) {
                f = Float.parseFloat((String) o);
            } else {
                throw new JsonException(context + " is not a valid float: " + o);
            }
        } catch (NumberFormatException | ArithmeticException e) {
            throw new JsonException(context + " is not a valid float: " + o, e);
        }

        if (Float.isNaN(f) || Float.isInfinite(f)) {
            throw new JsonException(context + " is not a finite float: " + o);
        }
        return f;
    }

    protected static double asDouble(Object o, String context) {
        checkNotNullish(o, context);
        double d;
        try {
            if (o instanceof Number) {
                d = ((Number) o).doubleValue();
            } else if (o instanceof String) {
                d = Double.parseDouble((String) o);
            } else {
                throw new JsonException(context + " is not a valid double: " + o);
            }
        } catch (NumberFormatException e) {
            throw new JsonException(context + " is not a valid double: " + o, e);
        }

        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new JsonException(context + " is not a finite double: " + o);
        }
        return d;
    }

    protected static BigInteger asBigInteger(Object o, String context) {
        checkNotNullish(o, context);
        try {
            if (o instanceof BigInteger) return (BigInteger) o;
            if (o instanceof Number) {
                if (o instanceof BigDecimal) {
                    return ((BigDecimal) o).toBigIntegerExact();
                }
                return BigInteger.valueOf(Maths.toLongExact((Number) o));
            }
            if (o instanceof String) {
                return new BigInteger((String) o);
            }
            throw new JsonException(context + " is not a valid integer: " + o);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new JsonException(context + " is not a valid integer: " + o, e);
        }
    }

    protected static BigDecimal asBigDecimal(Object o, String context) {
        checkNotNullish(o, context);
        try {
            if (o instanceof BigDecimal) return (BigDecimal) o;
            if (o instanceof Number) {
                if (o instanceof BigInteger) return new BigDecimal((BigInteger) o);
                return BigDecimal.valueOf(((Number) o).doubleValue());
            }
            if (o instanceof String) {
                return new BigDecimal((String) o);
            }
            throw new JsonException(context + " is not a valid decimal: " + o);
        } catch (NumberFormatException e) {
            throw new JsonException(context + " is not a valid decimal: " + o, e);
        }
    }

    protected static boolean asBoolean(Object o, String context) {
        checkNotNullish(o, context);
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof String) {
            String s = (String) o;
            if ("true".equalsIgnoreCase(s))  return true;
            if ("false".equalsIgnoreCase(s)) return false;
        }
        throw new JsonException(context + " is not a valid boolean: " + o);
    }

    protected static String asString(Object o, String context) {
        checkNotNullish(o, context);
        if (o instanceof String) return (String) o;
        throw new JsonException(context + " is not a valid string: " + o);
    }

    protected static char asChar(Object o, String context) {
        checkNotNullish(o, context);
        if (o instanceof String) {
            String s = (String) o;
            if (s.length() == 1) return s.charAt(0);
            throw new JsonException(context + " is not a single character: \"" + s + "\"");
        }
        throw new JsonException(context + " is not a valid char: " + o);
    }

    protected static JsonObject asObject(Object o, String context) {
        checkNotNullish(o, context);
        if (o instanceof JsonObject) return (JsonObject) o;
        throw new JsonException(context + " is not a JSON object: " + o);
    }

    protected static JsonArray asArray(Object o, String context) {
        checkNotNullish(o, context);
        if (o instanceof JsonArray) return (JsonArray) o;
        throw new JsonException(context + " is not a JSON array: " + o);
    }

    @SuppressWarnings("unchecked")
    protected static <T> List<T> asList(Object o, Class<T> clazz, String context) {
        checkNotNullish(o, context);
        Objects.requireNonNull(clazz, "clazz");

        Class<?> elementType = PrimitiveUtils.wrap(clazz);

        if (!(o instanceof Collection)) {
            throw new JsonException(context + " is not a collection: " + o);
        }

        List<T> result = new ArrayList<>();
        for (Object e : (Collection<?>) o) {
            if (e == NULL) {
                result.add(null);
                continue;
            }
            if (e != null && !elementType.isInstance(e)) {
                throw new JsonException(
                        "Element '" + e + "' of " + context + " is not a " + elementType.getName());
            }
            result.add((T) e);
        }
        return result;
    }

    protected static <T> Set<T> asSet(Object o, Class<T> clazz, String context) {
        List<T> list = asList(o, clazz, context);
        Set<T> set = new LinkedHashSet<>(list);
        if (set.size() != list.size()) {
            throw new JsonException(context + " contains duplicate elements");
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    protected static <K, V> Map<K, V> asMap(Object o, Class<K> keyType, Class<V> valueType, String context) {
        checkNotNullish(o, context);
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");

        Class<?> kt = PrimitiveUtils.wrap(keyType);
        Class<?> vt = PrimitiveUtils.wrap(valueType);

        if (!(o instanceof Map)) {
            throw new JsonException(context + " is not a map: " + o);
        }

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) o).entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            if (!kt.isInstance(k)) {
                throw new JsonException(
                        "Map key '" + k + "' of " + context + " is not a " + kt.getName());
            }
            if (v == NULL) {
                result.put((K) k, null);
                continue;
            }
            if (v != null && !vt.isInstance(v)) {
                throw new JsonException(
                        "Map value '" + v + "' of " + context + " is not a " + vt.getName());
            }
            result.put((K) k, (V) v);
        }
        return result;
    }
}
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
package io.github.gapplex.gxu.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

public final class Maths {
    private Maths() {
        throw new AssertionError("No instances");
    }

    public static short toShortExact(long value) {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new ArithmeticException("short overflow: " + value);
        }
        return (short) value;
    }

    public static byte toByteExact(long value) {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw new ArithmeticException("byte overflow: " + value);
        }
        return (byte) value;
    }

    public static long toLongExact(Number n) {
        Objects.requireNonNull(n, "n must not be null");
        if (n instanceof Long || n instanceof Integer
                || n instanceof Short || n instanceof Byte) {
            return n.longValue();
        }
        if (n instanceof BigInteger) {
            return ((BigInteger) n).longValueExact();
        }
        if (n instanceof BigDecimal) {
            return ((BigDecimal) n).setScale(0, RoundingMode.DOWN).longValueExact();
        }
        if (n instanceof Double || n instanceof Float) {
            return doubleToLongExact(n.doubleValue(), n);
        }
        try {
            return new BigDecimal(n.toString())
                    .setScale(0, RoundingMode.DOWN)
                    .longValueExact();
        } catch (NumberFormatException e) {
            throw new ArithmeticException("not a finite number: '" + n + "'");
        }
    }

    private static long doubleToLongExact(double d, Number source) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new ArithmeticException("not a finite number: " + source);
        }
        if (d < -0x1p63 || d >= 0x1p63) {
            throw new ArithmeticException("long overflow: " + source);
        }
        return (long) d;
    }
}
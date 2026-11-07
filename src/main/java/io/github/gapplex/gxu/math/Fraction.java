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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

public final class Fraction extends Number implements Comparable<Fraction>, Serializable {
    public static final Fraction ZERO = new Fraction(0, 1);
    public static final Fraction ONE = new Fraction(1, 1);
    public static final Fraction TEN = new Fraction(10, 1);

    private static final long serialVersionUID = 1L;
    private final BigInteger numerator;
    private final BigInteger denominator;

    public Fraction(long numerator, long denominator) {
        this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    public Fraction(BigInteger numerator, BigInteger denominator) {
        Objects.requireNonNull(numerator, "numerator");
        Objects.requireNonNull(denominator, "denominator");

        if (denominator.signum() == 0) {
            throw new ArithmeticException("Denominator cannot be 0");
        }

        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }

        BigInteger gcd = numerator.gcd(denominator);
        this.numerator = numerator.divide(gcd);
        this.denominator = denominator.divide(gcd);
    }

    public BigInteger getNumerator() {
        return numerator;
    }

    public BigInteger getDenominator() {
        return denominator;
    }

    public Fraction add(Fraction other) {
        return new Fraction(
                numerator.multiply(other.denominator)
                        .add(other.numerator.multiply(denominator)),
                denominator.multiply(other.denominator)
        );
    }

    public Fraction subtract(Fraction other) {
        return new Fraction(
                numerator.multiply(other.denominator)
                        .subtract(other.numerator.multiply(denominator)),
                denominator.multiply(other.denominator)
        );
    }

    public Fraction multiply(Fraction other) {
        return new Fraction(
                numerator.multiply(other.numerator),
                denominator.multiply(other.denominator)
        );
    }

    public Fraction divide(Fraction other) {
        if (other.numerator.signum() == 0) {
            throw new ArithmeticException("Divide by zero");
        }
        return new Fraction(
                numerator.multiply(other.denominator),
                denominator.multiply(other.numerator)
        );
    }

    @Override
    public int intValue() {
        return numerator.divide(denominator).intValue();
    }

    @Override
    public long longValue() {
        return numerator.divide(denominator).longValue();
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {
        return new BigDecimal(numerator)
                .divide(new BigDecimal(denominator), MathContext.DECIMAL128)
                .doubleValue();
    }

    @Override
    public int compareTo(Fraction other) {
        return numerator.multiply(other.denominator)
                .compareTo(other.numerator.multiply(denominator));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fraction)) return false;
        Fraction other = (Fraction) o;
        return numerator.equals(other.numerator)
                && denominator.equals(other.denominator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    @Override
    public String toString() {
        if (denominator.equals(BigInteger.ONE)) {
            return numerator.toString();
        }
        return numerator + "/" + denominator;
    }

    public Fraction abs(){
        return new Fraction(numerator.abs(), denominator.abs());
    }

    /**
     * 获取小数点后指定位数的十进制字符串表示
     *
     * @param scale 小数位数（必须 >= 0）
     * @return 十进制字符串，例如 "3.14159"
     * @throws IllegalArgumentException 如果 scale 为负数
     */
    public String toDecimalString(int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("Scale cannot be negative: " + scale);
        }

        // 使用 BigDecimal 进行精确除法，保留指定小数位数
        BigDecimal result = new BigDecimal(numerator)
                .divide(new BigDecimal(denominator), scale, RoundingMode.HALF_UP);

        return result.toPlainString();
    }

    public static Fraction parseFraction(String s) {
        Objects.requireNonNull(s, "s");
        s = s.trim();

        if (s.isEmpty()) {
            throw new NumberFormatException("Empty fraction string");
        }

        int slash = s.indexOf('/');

        if (slash < 0) {
            return new Fraction(new BigInteger(s), BigInteger.ONE);
        }

        if (slash != s.lastIndexOf('/')) {
            throw new NumberFormatException("Multiple '/' in fraction: " + s);
        }

        String numStr = s.substring(0, slash).trim();
        String denStr = s.substring(slash + 1).trim();

        if (numStr.isEmpty() || denStr.isEmpty()) {
            throw new NumberFormatException("Missing numerator or denominator: " + s);
        }

        try {
            return new Fraction(new BigInteger(numStr), new BigInteger(denStr));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid fraction: " + s);
        }
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream in)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static final class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final BigInteger numerator;
        private final BigInteger denominator;

        SerializationProxy(Fraction f) {
            this.numerator = f.numerator;
            this.denominator = f.denominator;
        }

        private Object readResolve() {
            return new Fraction(numerator, denominator);
        }
    }
}
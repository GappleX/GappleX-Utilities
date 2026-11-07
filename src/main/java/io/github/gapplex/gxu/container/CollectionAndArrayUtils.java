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

import java.util.*;
import java.util.function.ObjIntConsumer;

public class CollectionAndArrayUtils {
    @FunctionalInterface
    public interface IntIntCsm{
        void accept(int o, int t);
    }

    @FunctionalInterface
    public interface ByteIntCsm{
        void accept(byte o, int t);
    }

    @FunctionalInterface
    public interface ShortIntCsm{
        void accept(short o, int t);
    }

    @FunctionalInterface
    public interface LongIntCsm{
        void accept(long o, int t);
    }

    @FunctionalInterface
    public interface DoubleIntCsm{
        void accept(double o, int t);
    }

    @FunctionalInterface
    public interface FloatIntCsm{
        void accept(float o, int t);
    }

    @FunctionalInterface
    public interface CharIntCsm{
        void accept(char o, int t);
    }

    @FunctionalInterface
    public interface BooleanIntCsm{
        void accept(boolean o, int t);
    }

    private CollectionAndArrayUtils(){}

    public static <T, R extends Collection<? super T>> R addAllAndNotNull(R c, T[] src){
        Objects.requireNonNull(c, "c");
        Objects.requireNonNull(src, "src");
        for (int i = 0; i < src.length; i++) {
            Objects.requireNonNull(src[i], "Null at src[" + i + "]");
        }
        c.addAll(Arrays.asList(src));
        return c;
    }

    public static <T> void foreach(T[] array, ObjIntConsumer<T> csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(int[] array, IntIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(short[] array, ShortIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(byte[] array, ByteIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(long[] array, LongIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(char[] array, CharIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(boolean[] array, BooleanIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(double[] array, DoubleIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }

    public static void foreach(float[] array, FloatIntCsm csm){
        Objects.requireNonNull(array, "array");
        Objects.requireNonNull(csm, "csm");
        for (int i = 0; i < array.length; i++) {
            csm.accept(array[i], i);
        }
    }
}

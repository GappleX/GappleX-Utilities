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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public interface IUnsafe {
    int getInt(Object o, long offset);

    void putInt(Object o, long offset, int x);

    Object getObject(Object o, long offset);

    void putObject(Object o, long offset, Object x);

    boolean getBoolean(Object o, long offset);

    void putBoolean(Object o, long offset, boolean x);

    byte getByte(Object o, long offset);

    void putByte(Object o, long offset, byte x);

    short getShort(Object o, long offset);

    void putShort(Object o, long offset, short x);

    char getChar(Object o, long offset);

    void putChar(Object o, long offset, char x);

    long getLong(Object o, long offset);

    void putLong(Object o, long offset, long x);

    float getFloat(Object o, long offset);

    void putFloat(Object o, long offset, float x);

    double getDouble(Object o, long offset);

    void putDouble(Object o, long offset, double x);

    byte getByte(long address);

    void putByte(long address, byte x);

    short getShort(long address);

    void putShort(long address, short x);

    char getChar(long address);

    void putChar(long address, char x);

    int getInt(long address);

    void putInt(long address, int x);

    long getLong(long address);

    void putLong(long address, long x);

    float getFloat(long address);

    void putFloat(long address, float x);

    double getDouble(long address);

    void putDouble(long address, double x);

    long getAddress(long address);

    void putAddress(long address, long x);

    long allocateMemory(long bytes);

    long reallocateMemory(long address, long bytes);

    void setMemory(Object o, long offset, long bytes, byte value);

    void setMemory(long address, long bytes, byte value);

    void copyMemory(Object srcBase, long srcOffset,
                    Object destBase, long destOffset,
                    long bytes);

    void copyMemory(long srcAddress, long destAddress, long bytes);

    void freeMemory(long address);

    long objectFieldOffset(Field f);

    long staticFieldOffset(Field f);

    Object staticFieldBase(Field f);

    boolean shouldBeInitialized(Class<?> c);

    void ensureClassInitialized(Class<?> c);

    int arrayBaseOffset(Class<?> arrayClass);

    int arrayIndexScale(Class<?> arrayClass);

    int addressSize();

    int pageSize();

    Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches);

    Object allocateInstance(Class<?> cls) throws InstantiationException;

    void throwException(Throwable ee);

    boolean compareAndSwapObject(Object o, long offset,
                                 Object expected,
                                 Object x);

    boolean compareAndSwapInt(Object o, long offset,
                              int expected,
                              int x);

    boolean compareAndSwapLong(Object o, long offset,
                               long expected,
                               long x);

    Object getObjectVolatile(Object o, long offset);

    void putObjectVolatile(Object o, long offset, Object x);

    int getIntVolatile(Object o, long offset);

    void putIntVolatile(Object o, long offset, int x);

    boolean getBooleanVolatile(Object o, long offset);

    void putBooleanVolatile(Object o, long offset, boolean x);

    byte getByteVolatile(Object o, long offset);

    void putByteVolatile(Object o, long offset, byte x);

    short getShortVolatile(Object o, long offset);

    void putShortVolatile(Object o, long offset, short x);

    char getCharVolatile(Object o, long offset);

    void putCharVolatile(Object o, long offset, char x);

    long getLongVolatile(Object o, long offset);

    void putLongVolatile(Object o, long offset, long x);

    float getFloatVolatile(Object o, long offset);

    void putFloatVolatile(Object o, long offset, float x);

    double getDoubleVolatile(Object o, long offset);

    void putDoubleVolatile(Object o, long offset, double x);

    void putOrderedObject(Object o, long offset, Object x);

    void putOrderedInt(Object o, long offset, int x);

    void putOrderedLong(Object o, long offset, long x);

    void unpark(Object thread);

    void park(boolean isAbsolute, long time);

    int getLoadAverage(double[] loadavg, int nelems);

    int getAndAddInt(Object o, long offset, int delta);

    long getAndAddLong(Object o, long offset, long delta);

    int getAndSetInt(Object o, long offset, int newValue);

    long getAndSetLong(Object o, long offset, long newValue);

    Object getAndSetObject(Object o, long offset, Object newValue);

    void loadFence();

    void storeFence();

    void fullFence();

    void invokeCleaner(ByteBuffer directBuffer);
}
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

public class CapacityUtils {
    private CapacityUtils(){}

    public static int capacity(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize cannot be negative: " + expectedSize);
        }
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        if (expectedSize < 1 << 30) {
            return expectedSize + expectedSize / 3;
        }
        return Integer.MAX_VALUE;
    }

    public static int inverseCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }

        if (capacity <= 3) {
            return capacity - 1;
        }

        if (capacity == Integer.MAX_VALUE) {
            return 1 << 30;
        }

        return getExpectedSize(capacity);
    }

    private static int getExpectedSize(int capacity) {
        if (capacity > 1431655764) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }

        int q = capacity / 4;
        int r = capacity % 4;

        if (r == 3) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }

        int expectedSize = 3 * q + r;

        if (capacity(expectedSize) != capacity) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }
        return expectedSize;
    }
}

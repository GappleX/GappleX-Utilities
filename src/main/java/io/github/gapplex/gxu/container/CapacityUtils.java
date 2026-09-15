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

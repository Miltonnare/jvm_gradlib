package com.aura.internal;

/**
 * Array helper operations for byte manipulation.
 */
public final class ArrayUtils {

    private ArrayUtils() {
        // Prevent instantiation
    }

    /**
     * Finds the index of the first occurrence of target pattern in target array.
     */
    public static int indexOf(byte[] array, byte[] target) {
        Preconditions.checkNotNull(array, "Array cannot be null");
        Preconditions.checkNotNull(target, "Target cannot be null");

        if (target.length == 0) {
            return 0;
        }

        outer:
        for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * Checks if target array contains the search pattern.
     */
    public static boolean contains(byte[] array, byte[] target) {
        return indexOf(array, target) != -1;
    }
}

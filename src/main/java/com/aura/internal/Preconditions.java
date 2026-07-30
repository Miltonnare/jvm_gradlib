package com.aura.internal;

/**
 * Utility methods for checking preconditions on arguments and state.
 */
public final class Preconditions {

    private Preconditions() {
        // Prevent instantiation
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void checkState(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

    public static void checkPositionIndexes(int start, int end, int size) {
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(
                String.format("Invalid range [%d, %d) for size %d", start, end, size)
            );
        }
    }
}

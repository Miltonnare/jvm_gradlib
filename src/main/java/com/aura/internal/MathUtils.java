package com.aura.internal;

/**
 * Basic mathematical operations for page coordinate scaling and margins.
 */
public final class MathUtils {

    private MathUtils() {
        // Prevent instantiation
    }

    /**
     * Clamps an integer value within the specified range.
     */
    public static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    /**
     * Clamps a double value within the specified range.
     */
    public static double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}

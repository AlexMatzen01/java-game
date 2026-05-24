package util;

public final class MathUtil {
    private MathUtil() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static int floorDiv(int value, int divisor) {
        int quotient = value / divisor;
        int remainder = value % divisor;
        if ((remainder != 0) && ((value ^ divisor) < 0)) {
            quotient--;
        }
        return quotient;
    }

    public static int floorMod(int value, int divisor) {
        int result = value % divisor;
        if ((result < 0 && divisor > 0) || (result > 0 && divisor < 0)) {
            result += divisor;
        }
        return result;
    }
}

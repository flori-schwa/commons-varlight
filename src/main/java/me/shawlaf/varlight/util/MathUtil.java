package me.shawlaf.varlight.util;

public final class MathUtil {

    private MathUtil() {

    }

    public static int modulo(int a, int b) {
        return ((a % b) + b) % b;
    }
}

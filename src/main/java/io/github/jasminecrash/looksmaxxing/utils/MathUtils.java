package io.github.jasminecrash.looksmaxxing.utils;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public abstract class MathUtils {
    public static Matrix4fc createFrustumMatrix(float left, float right, float bottom, float top, float near, float far) {
        return new Matrix4f(
                2*near/(right-left), 0, 0, -near*(right+left)/(right-left),
                0, 2*near/(top-bottom), 0, -near*(top+bottom)/(top-bottom),
                0, 0, -(far+near)/(far-near), 2*far*near/(near-far),
                0, 0, -1, 0
        );
    }
}

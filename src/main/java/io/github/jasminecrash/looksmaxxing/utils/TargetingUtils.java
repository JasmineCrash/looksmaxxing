package io.github.jasminecrash.looksmaxxing.utils;

import org.joml.FrustumIntersection;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

public abstract class TargetingUtils {
    public static boolean isVectorInFrustum(Vector3fc vec, Matrix4fc frustumMatrix) {
        FrustumIntersection intersectionChecker = new FrustumIntersection(frustumMatrix);
        return intersectionChecker.testPoint(vec);
    }
    public static boolean isVectorInFrustum(Vector3fc vec, Frustum frustum) {
        return frustum.testIntersection(vec);
    }
}

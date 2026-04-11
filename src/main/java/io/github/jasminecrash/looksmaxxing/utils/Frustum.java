package io.github.jasminecrash.looksmaxxing.utils;

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

public class Frustum {
    private final FrustumIntersection intersectionChecker;
    private final FrustumRayBuilder rayCaster;

    public Frustum(Matrix4fc frustumMatrix) {
        this.intersectionChecker = new FrustumIntersection(frustumMatrix);
        this.rayCaster = new FrustumRayBuilder(frustumMatrix);
    }

    public void updateFrustum(Matrix4fc frustumMatrix) {
        this.intersectionChecker.set(frustumMatrix);
        this.rayCaster.set(frustumMatrix);
    }

    public boolean testIntersection(Vector3fc vec) {
        return this.intersectionChecker.testPoint(vec);
    }
    public boolean testIntersection(Vec3 vec) {
        return this.intersectionChecker.testPoint(vec.toVector3f());
    }
}

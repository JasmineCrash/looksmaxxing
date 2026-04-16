package io.github.jasminecrash.looksmaxxing.utils;

import net.minecraft.world.phys.Vec3;
import org.joml.*;

public class Frustum {
    private final FrustumIntersection intersectionChecker;
    private final FrustumRayBuilder rayCaster;
    public final Matrix4f projectionMatrix;

    public Frustum(Matrix4fc frustumMatrix) {
        this.intersectionChecker = new FrustumIntersection(frustumMatrix);
        this.rayCaster = new FrustumRayBuilder(frustumMatrix);
        this.projectionMatrix = new Matrix4f(frustumMatrix);
    }
    public Frustum(
            Vector3f position,
            Vector3f look,
            Vector3f up,
            float alphaRad,
            float betaRad,
            float near,
            float far
    ) {
        Matrix4f frustumMatrix = MathUtils.createFrustumMatrix(position, look, up, alphaRad, betaRad, near, far);
        this.intersectionChecker = new FrustumIntersection(frustumMatrix);
        this.rayCaster = new FrustumRayBuilder(frustumMatrix);
        this.projectionMatrix = new Matrix4f(frustumMatrix);
    }

    public void updateFrustum(Matrix4fc frustumMatrix) {
        this.intersectionChecker.set(frustumMatrix);
        this.rayCaster.set(frustumMatrix);
        this.projectionMatrix.set(frustumMatrix);
    }

    public boolean testIntersection(Vector3fc vec) { return this.intersectionChecker.testPoint(vec); }
    public boolean testIntersection(Vec3 vec) { return testIntersection(vec.toVector3f()); }

    public Vector3f[] getCorners() {
        Matrix4f inv = this.projectionMatrix.invert(new Matrix4f());
        float[][] ndcCorners = {
                {-1, -1, -1, 1},  // near bottom-left
                { 1, -1, -1, 1},  // near bottom-right
                {-1,  1, -1, 1},  // near top-left
                { 1,  1, -1, 1},  // near top-right
                {-1, -1,  1, 1},  // far bottom-left
                { 1, -1,  1, 1},  // far bottom-right
                {-1,  1,  1, 1},  // far top-left
                { 1,  1,  1, 1},  // far top-right
        };

        Vector3f[] corners = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            float[] c = ndcCorners[i];
            corners[i] = new Vector3f();
            inv.transformProject(c[0], c[1], c[2], corners[i]);
        }
        return corners;
    }
}

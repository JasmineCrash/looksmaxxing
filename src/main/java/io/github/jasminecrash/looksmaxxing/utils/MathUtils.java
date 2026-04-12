package io.github.jasminecrash.looksmaxxing.utils;

import org.joml.*;
import org.joml.Math;

public abstract class MathUtils {
    public static Matrix4f createFrustumMatrix(
            Vector3f position,
            Vector3f look,
            Vector3f up,
            float alphaRad,
            float betaRad,
            float near,
            float far
    ) {
        float aspect = (float) (Math.tan(alphaRad / 2.0) / Math.tan(betaRad / 2.0));
        Vector3f target = new Vector3f(position).add(look);
        Matrix4f view = new Matrix4f().lookAt(position, target, up);
        Matrix4f proj = new Matrix4f().perspective(betaRad, aspect, near, far);
        return proj.mul(view);
    }

    public static Vector3f[] generateFrustumSurface(Frustum frustum) {
        if (frustum == null) { throw new IllegalArgumentException("Cannot generate points for the mesh of a null frustum"); }
        Vector3f[] surface = new Vector3f[24]; //packed positions of the four points on each of the 6 quads
        Vector3f[] corners = frustum.getCorners();
        int i = 0;
        surface[i++] = corners[0]; surface[i++] = corners[1]; surface[i++] = corners[3]; surface[i++] = corners[2];  //near clip plane
        surface[i++] = corners[4]; surface[i++] = corners[5]; surface[i++] = corners[7]; surface[i++] = corners[6];  //far clip plane
        surface[i++] = corners[0]; surface[i++] = corners[2]; surface[i++] = corners[6]; surface[i++] = corners[4];  //left plane
        surface[i++] = corners[1]; surface[i++] = corners[3]; surface[i++] = corners[7]; surface[i++] = corners[5];  //right plane
        surface[i++] = corners[0]; surface[i++] = corners[1]; surface[i++] = corners[5]; surface[i++] = corners[4];  //bottom plane
        surface[i++] = corners[2]; surface[i++] = corners[3]; surface[i++] = corners[7]; surface[i++] = corners[6];  //top plane (poor little i++...)
        return surface;
    }

}

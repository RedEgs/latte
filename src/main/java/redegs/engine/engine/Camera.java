package redegs.engine.engine;

import org.joml.Matrix4f;

public class Camera {

    public static Matrix4f perspectiveMatrix(float fov, float aspect_ratio, float near_plane, float far_plane) {
        Matrix4f mat = new Matrix4f().identity();
        mat.perspective(fov, aspect_ratio, near_plane, far_plane);
        return mat;
    }

    public static Matrix4f perspectiveDefaultMatrix(int width, int height) {
        float fov = (float) Math.toRadians(80.0f); // Field of View in radians
        float aspectRatio = (float) width / height; // Window dimensions
        float nearPlane = 0.1f;  // Closest visible distance
        float farPlane = 1000.0f; // Farthest visible distance

        return perspectiveMatrix(fov, aspectRatio, nearPlane, farPlane);
    }
}

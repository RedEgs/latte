package redegs.engine.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    protected Matrix4f view;
    protected Matrix4f projection;
    protected Float fov;

    protected Integer width, height;

    public Camera(int width, int height) {
        view = new Matrix4f().identity();
        projection =  perspectiveDefaultMatrix(width, height);
        fov = projection.perspectiveFov();

        this.width = width;
        this.height = height;
    }

    public void Update(double delta_time, double elapsed_time) {}

    public void rotateViewByAngle(Float angle, Vector3f vec) {
        this.view.rotate(angle, vec);
    }

    public void translateViewPosition(Vector3f pos) {
        this.view.translate(pos);
    }

    public void setFov(Float fov, Integer width, Integer height) {
        this.fov = fov;

        Float rfov = (float) Math.toRadians(fov); // Field of View in radians
        float aspectRatio = (float) width / height; // Window dimensions
        float nearPlane = 0.1f;  // Closest visible distance
        float farPlane = 1000.0f; // Farthest visible distance

        this.projection = perspectiveMatrix(rfov, aspectRatio, nearPlane, farPlane);
    }
    public void setFov(Float fov) {
        this.setFov(fov, this.width, this.height);
    }

    public Float getFov() {
        return fov;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Matrix4f getView() {
        return view;
    }

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

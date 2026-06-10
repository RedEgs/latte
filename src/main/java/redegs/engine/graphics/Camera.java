package redegs.engine.graphics;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.Engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;

public class Camera {
    protected Matrix4f view;
    protected Matrix4f projection;
    protected Float fov;

    protected boolean mouse_locked = false;
    protected Transform transform;
    protected Integer width, height;

    public Camera(int width, int height) {
        view = new Matrix4f().identity();
        projection = perspectiveDefaultMatrix(width, height);
        fov = projection.perspectiveFov();

        transform = new Transform();

        this.width = width;
        this.height = height;
    }

    public void Update(double delta_time, double elapsed_time) {}

    public void onKeyPress(int key, int scancode, int action, int mods) {

    }

    public void rotateViewByAngle(Float angle, Vector3f vec) {
        this.view.rotate(angle, vec);
    }

    public void translateViewPosition(Vector3f pos) {
        this.transform.position.add(pos);

        this.view.identity();
        this.view.translate(
                -this.transform.position.x,
                -this.transform.position.y,
                -this.transform.position.z
        );
    }

    public void setFov(Float fov, Integer width, Integer height) {
        this.fov = fov;

        Float rfov = (float) Math.toRadians(fov);
        float aspectRatio = (float) width / height;
        float nearPlane = 0.1f;
        float farPlane = 1000.0f;

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
        float fov = (float) Math.toRadians(80.0f);
        float aspectRatio = (float) width / height;
        float nearPlane = 0.1f;
        float farPlane = 1000.0f;

        return perspectiveMatrix(fov, aspectRatio, nearPlane, farPlane);
    }

    public void setPosition(Vector3f pos) {
        this.transform.position.set(pos);

        this.view.identity();
        this.view.translate(
                this.transform.position.x,
                this.transform.position.y,
                this.transform.position.z
        );
    }

    public Transform getTransform() {
        return this.transform;
    }

    public Vector3f getPosition() {
        return this.transform.position;
    }

    public Matrix3f getNormalMatrix() {
        // Extract the 3x3 rotation/scale part from the view matrix
        // For view matrix, we want the inverse transpose of the view's rotation
        Matrix3f normalMatrix = new Matrix3f();
        this.view.get3x3(normalMatrix);
        return normalMatrix.invert().transpose();
    }

    public Matrix4f getStaticViewMatrix() {
        return new Matrix4f(new Matrix3f(view));
    }

    public void unlockMouse() {
        glfwSetInputMode(Engine.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouse_locked = false;
    }

    public void lockMouse() {
        glfwSetInputMode(Engine.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        mouse_locked = true;
    }

    public void toggleMouseLock() {
        if (mouse_locked) {
            unlockMouse();
        } else {
            lockMouse();
        }
    }
}
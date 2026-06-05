package redegs.engine.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.Engine;

import static org.lwjgl.glfw.GLFW.*;

public class ControllableCamera extends Camera {
    protected Vector3f position;
    protected Vector3f front = new Vector3f(0, 0, -1);
    protected Vector3f up = new Vector3f(0, 1, 0);
    protected Vector3f right = new Vector3f(1, 0, 0);
    protected Vector3f worldUp = new Vector3f(0, 1, 0);

    public Float speed = 5.0f;

    private double mouseX = 0.0, mouseY = 0.0;

    private float yaw = -90.0f;   // Looking left/right
    private float pitch = 0.0f;   // Looking up/down

    private boolean first_mouse = true;
    private float mouse_sensitivity = 0.1f;
    private boolean[] keys = new boolean[GLFW_KEY_LAST + 1];

    public ControllableCamera(int width, int height) {
        super(width, height);

        position = new Vector3f();
        updateVectors(); // Initialize vectors

        glfwSetKeyCallback(Engine.getWindow(), (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                keys[key] = true;
            } else if (action == GLFW_RELEASE) {
                keys[key] = false;
            }
        });

        glfwSetInputMode(Engine.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    @Override
    public void Update(double delta_time, double elapsed_time) {
        super.Update(delta_time, elapsed_time);

        handleKeyboard(delta_time);
        handleMouse(delta_time);
        updateViewMatrix();
    }

    private void handleKeyboard(double delta_time) {
        Float curr_speed = speed * (float) delta_time;

        // Move forward/backward relative to where camera is facing
        if (keys[GLFW_KEY_W]) {
            position.fma(curr_speed, front);
        }

        if (keys[GLFW_KEY_S]) {
            position.fma(-curr_speed, front);
        }

        if (keys[GLFW_KEY_D]) {
            position.fma(curr_speed, right);
        }

        if (keys[GLFW_KEY_A]) {
            position.fma(-curr_speed, right);
        }

        // Vertical movement (world up, not camera up)
        if (keys[GLFW_KEY_SPACE]) position.y -= curr_speed;
        if (keys[GLFW_KEY_LEFT_SHIFT]) position.y += curr_speed;
    }

    private void handleMouse(double delta_time) {
        double[] xpos = new double[1];
        double[] ypos = new double[1];

        glfwGetCursorPos(Engine.getWindow(), xpos, ypos);

        if (first_mouse) {
            mouseX = xpos[0];
            mouseY = ypos[0];
            first_mouse = false;
            return;
        }

        double xoffset = xpos[0] - mouseX;
        double yoffset = mouseY - ypos[0];
        mouseX = xpos[0];
        mouseY = ypos[0];

        xoffset *= mouse_sensitivity;
        yoffset *= mouse_sensitivity;

        yaw += xoffset;
        pitch += yoffset;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateVectors();
    }

    private void updateVectors() {
        front.x = (float)(Math.cos(Math.toRadians(yaw))
                * Math.cos(Math.toRadians(pitch)));
        front.y = (float)Math.sin(Math.toRadians(pitch));
        front.z = (float)(Math.sin(Math.toRadians(yaw))
                * Math.cos(Math.toRadians(pitch)));

        front.normalize();

        front.cross(worldUp, right);
        right.normalize();

        right.cross(front, up);
        up.normalize();
    }

    private void updateViewMatrix() {
        Vector3f center = new Vector3f(position).add(front);

        view.identity();
        view.lookAt(position, center, up);
    }

    public Vector3f getPosition() {
        return this.position;
    }

    // Getter/setter for mouse sensitivity
    public void setMouseSensitivity(float sensitivity) {
        this.mouse_sensitivity = sensitivity;
    }

    // Reset mouse position (useful when regaining focus)
    public void resetMousePosition() {
        first_mouse = true;
    }
}
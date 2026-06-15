package redegs.engine.engine.components;

import org.joml.Vector3f;
import redegs.Engine;
import redegs.engine.engine.events.KeyPressEvent;
import redegs.engine.graphics.Camera;

import static org.lwjgl.glfw.GLFW.*;

public class ControllableCamera extends Camera {
    protected Vector3f front = new Vector3f(0, 0, -1);
    protected Vector3f up = new Vector3f(0, 1, 0);
    protected Vector3f right = new Vector3f(1, 0, 0);
    protected Vector3f worldUp = new Vector3f(0, 1, 0);

    public Float speed = 5.0f;

    private double mouseX = 0.0, mouseY = 0.0;

    private boolean first_mouse = true;
    private float mouse_sensitivity = 0.1f;
    private boolean[] keys = new boolean[GLFW_KEY_LAST + 1];

    public ControllableCamera(int width, int height) {
        super(width, height);
        name = "ControllableCameraComponent";

        transform.rotation.y = -90.0f;
        transform.rotation.x = 0.0f;

        updateVectors();      // initializes front/up/right
        updateViewMatrix();   // now safe, fields are set

        lockMouse();
        first_mouse = true;
    }

    @Override
    public void Update(double delta_time, double elapsed_time) {
        super.Update(delta_time, elapsed_time);
        if (mouse_locked) {
            handleKeyboard(delta_time);
            handleMouse(delta_time);
        }
        updateViewMatrix();
    }

    public void onKeyPress(KeyPressEvent event) {
        super.onKeyPress(event);

        if (event.action == GLFW_PRESS) {
            keys[event.key] = true;

            if (event.key == GLFW_KEY_TAB) {
                toggleMouseLock();
            }

        } else if (event.action == GLFW_RELEASE) {
            keys[event.key] = false;
        }
    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();
    }

    private void handleKeyboard(double delta_time) {
        Float curr_speed = speed * (float) delta_time;

        // Move forward/backward relative to where camera is facing
        if (keys[GLFW_KEY_W]) {
            transform.position.fma(curr_speed, front);
        }

        if (keys[GLFW_KEY_S]) {
            transform.position.fma(-curr_speed, front);
        }

        if (keys[GLFW_KEY_D]) {
            transform.position.fma(curr_speed, right);
        }

        if (keys[GLFW_KEY_A]) {
            transform.position.fma(-curr_speed, right);
        }

        // Vertical movement (world up, not camera up)
        if (keys[GLFW_KEY_SPACE]) transform.position.y -= curr_speed;
        if (keys[GLFW_KEY_LEFT_SHIFT]) transform.position.y += curr_speed;
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

        // yaw -> rotation.y, pitch -> rotation.x
        transform.rotation.y += (float) xoffset;
        transform.rotation.x += (float) yoffset;

        if (transform.rotation.x > 89.0f) transform.rotation.x = 89.0f;
        if (transform.rotation.x < -89.0f) transform.rotation.x = -89.0f;

        updateVectors();
    }

    private void updateVectors() {
        float yaw = transform.rotation.y;
        float pitch = transform.rotation.x;

        front.x = (float) (Math.cos(Math.toRadians(yaw))
                * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw))
                * Math.cos(Math.toRadians(pitch)));

        front.normalize();

        front.cross(worldUp, right);
        right.normalize();

        right.cross(front, up);
        up.normalize();
    }

    @Override
    protected void updateViewMatrix() {
        Vector3f center = new Vector3f(transform.position).add(front);

        view.identity();
        view.lookAt(transform.position, center, up);
    }

    // Getter/setter for mouse sensitivity
    public void setMouseSensitivity(float sensitivity) {
        this.mouse_sensitivity = sensitivity;
    }

    // Reset mouse position (useful when regaining focus)
    public void resetMousePosition() {
        first_mouse = true;
    }

    @Override
    public void lockMouse() {
        super.lockMouse();
        first_mouse = true;
    }
}
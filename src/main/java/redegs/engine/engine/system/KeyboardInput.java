package redegs.engine.engine.system;

import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;


public class KeyboardInput {
    private long window;

    // Key callback
    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }

            // Handle specific keys
            if (action == GLFW_PRESS) {
                System.out.println("Key pressed: " + key);
            } else if (action == GLFW_RELEASE) {
                System.out.println("Key released: " + key);
            } else if (action == GLFW_REPEAT) {
                System.out.println("Key held: " + key);
            }
        }
    };

    public void init() {
        // Set the callback
        glfwSetKeyCallback(window, keyCallback);
    }
}
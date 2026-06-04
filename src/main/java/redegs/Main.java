package redegs;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import redegs.engine.engine.Camera;
import redegs.engine.graphics.*;
import redegs.engine.util.GLException;

import java.nio.*;

import static java.lang.Math.sin;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static redegs.engine.util.Debug.glCheckError;
import static redegs.engine.util.Types.sizeof;


public class Main {

    // The window handle
    private long window;
    Shader shader;
    int screenWidth = 1280;
    int screenHeight = 720;

    public void run() throws GLException {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);


        // Create the window
        window = glfwCreateWindow(screenWidth, screenHeight, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() throws GLException {

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        glViewport(0, 0, screenWidth, screenHeight);
        glEnable(GL_DEPTH_TEST); // CRITICAL: Enable depth testing!

        // Set the clear color
        shader = new Shader(Shader.FragmentShader_camera, Shader.VertexShader_camera);

        Matrix4f model = new Matrix4f().identity();
        Matrix4f view = new Matrix4f().identity();
        Matrix4f proj = new Matrix4f().identity();

        // Move camera back (negative Z direction)
        view.translate(0, 0, -3); // Changed from -10 to -3 (closer)

        // Set up perspective projection
        float aspect = (float) screenWidth / (float) screenHeight;
        proj.perspective((float) Math.toRadians(60), aspect, 0.1f, 100.0f);

        UniformBuffer ubo = new UniformBuffer("CameraInfo", shader, 128, 0);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Use FloatBuffer for matrix data, not ByteBuffer
            FloatBuffer projBuffer = stack.mallocFloat(16);
            FloatBuffer viewBuffer = stack.mallocFloat(16);
            FloatBuffer combinedBuffer = stack.mallocFloat(32); // 16 + 16 floats

            proj.get(projBuffer);
            view.get(viewBuffer);

            // Combine both matrices
            combinedBuffer.put(projBuffer);
            combinedBuffer.put(viewBuffer);
            combinedBuffer.flip(); // IMPORTANT: flip before sending to OpenGL

            // Now convert to ByteBuffer for the update method
            // This assumes your update() method expects ByteBuffer
            ByteBuffer byteBuffer = stack.malloc(128);
            for (int i = 0; i < 32; i++) {
                byteBuffer.putFloat(combinedBuffer.get(i));
            }
            byteBuffer.flip();

            ubo.update(byteBuffer);
        }

//
//        VertexArray vao = new VertexArray();
//        vao.bind();
//
//        VertexBuffer vbo = new VertexBuffer();
//        FloatBuffer vertices = VertexBuffer.texture_triangle();
//        vbo.setBufferData(vertices);
//        vbo.setLayout(0, 3, GL_FLOAT, 8 * 4);
//        vbo.setLayout(1, 3, GL_FLOAT, false, 8 * 4, 3*4);
//        vbo.setLayout(2, 2, GL_FLOAT, false, 8 * 4, 6*4);
        Mesh mesh = Mesh.quad();
        Texture texture = new Texture("F:/Programming/Java/Engine/Latte/src/main/resources/brick.jpg");

        glCheckError();


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.



        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

            shader.use();

            model.rotate((float)( glfwGetTime() / glfwGetTime()) / 100, new Vector3f(0, 1, 0));
            try (MemoryStack stack = MemoryStack.stackPush()) {
//                FloatBuffer projBuffer = stack.mallocFloat(16);
//                FloatBuffer viewBuffer = stack.mallocFloat(16);
                FloatBuffer modelBuffer = stack.mallocFloat(16);

//                proj.get(projBuffer);
//                view.get(viewBuffer);
                model.get(modelBuffer);

//                shader.setUniformMat4("proj", projBuffer);
//                shader.setUniformMat4("view", viewBuffer);
                shader.setUniformMat4("model", modelBuffer);
            }


//            view.translate(0, 0, (float) Math.sin(glfwGetTime() / 1000.0) * -3); // Changed from -10 to -3 (closer)
//
//            try (MemoryStack stack = MemoryStack.stackPush()) {
//                // Use FloatBuffer for matrix data, not ByteBuffer
//                FloatBuffer projBuffer = stack.mallocFloat(16);
//                FloatBuffer viewBuffer = stack.mallocFloat(16);
//                FloatBuffer combinedBuffer = stack.mallocFloat(32); // 16 + 16 floats
//
//                proj.get(projBuffer);
//                view.get(viewBuffer);
//
//                // Combine both matrices
//                combinedBuffer.put(projBuffer);
//                combinedBuffer.put(viewBuffer);
//                combinedBuffer.flip(); // IMPORTANT: flip before sending to OpenGL
//
//                // Now convert to ByteBuffer for the update method
//                // This assumes your update() method expects ByteBuffer
//                ByteBuffer byteBuffer = stack.malloc(128);
//                for (int i = 0; i < 32; i++) {
//                    byteBuffer.putFloat(combinedBuffer.get(i));
//                }
//                byteBuffer.flip();
//
//                ubo.update(byteBuffer);
//            }
//
//            texture.use(shader, 0);
//            vao.bind();
//            glDrawArrays(GL_TRIANGLES, 0, 3);

            mesh.Draw(shader);

            glCheckError();
            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}
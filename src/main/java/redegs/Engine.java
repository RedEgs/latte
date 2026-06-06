package redegs;

import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import redegs.engine.engine.Camera;
import redegs.engine.engine.ControllableCamera;
import redegs.engine.engine.Scene;
import redegs.engine.engine.SceneManager;
import redegs.engine.graphics.*;
import redegs.engine.graphics.lights.PointLightSource;
import redegs.engine.graphics.pipelines.DeferredPipeline;
import redegs.engine.util.GLException;

import java.nio.*;

import static java.lang.Math.sin;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBFramebufferSRGB.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_OUTPUT_SYNCHRONOUS;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static redegs.engine.util.Debug.glCheckError;
import static redegs.engine.util.Types.sizeof;


public class Engine {
    // The window handle
    private static Engine INSTANCE;
    private static Long window;

    private final Float version = 0.2f;
    private String name = "Latte" + " " + String.valueOf(version);


    private final int screenWidth = 1280;
    private final int screenHeight = 720;

    private double last_time, delta_time;
    SceneManager scene_manager = SceneManager.getInstance();

    private void Init() {
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
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);


        // Create the window
        window = glfwCreateWindow(screenWidth, screenHeight, name, NULL, NULL);
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
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glViewport(0, 0, screenWidth, screenHeight);
        glEnable(GL_DEPTH_TEST); // CRITICAL: Enable depth testing!
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glEnable(GL_FRAMEBUFFER_SRGB);
        glEnable(GL_CULL_FACE);

    }

    private void Loop() throws GLException {

        Renderer<DeferredPipeline> renderer = new Renderer<>(DeferredPipeline::new);
        scene_manager.setRenderer(renderer);

        ControllableCamera camera = new ControllableCamera(screenWidth, screenHeight);
        camera.setPosition(new Vector3f(0, 0, 3f));

        Scene main = new Scene(camera);

        Material m = Material.fromTexture(new Texture("src/main/resources/brick.jpg"));
        Mesh mesh = Mesh.cube();
        mesh.AddMaterial(m);
        Model model = Model.fromMesh(mesh);
        main.addModel(model);

        m = Material.fromTexture(new Texture("src/main/resources/brick.jpg"));
        mesh = Mesh.cube();
        mesh.AddMaterial(m);
        Model model2 = Model.fromMesh(mesh);
        main.addModel(model2);

        for (int i = 0; i < 10; i++) {
            int s = 3;
            Random rand = new Random();

            Float x = (float) rand.nextInt(s);
            Float y = (float) rand.nextInt(s);
            Float z = (float) rand.nextInt(s);

            Float colorx = rand.nextFloat();
            Float colory = rand.nextFloat();
            Float colorz = rand.nextFloat();

            model2.getModelMatrix().translate(new Vector3f(x, y, z));
            main.addLight(new PointLightSource(new Vector3f(x, y, z), new Vector3f(colorx, colory, colorz), 5f, 3f));
        }
        scene_manager.AddScene(main, "main");







        glCheckError();

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

            onKeyPress();

            calculateDT();
            scene_manager.Execute(delta_time, glfwGetTime());





            //System.out.println(main.getLights().size());

            glCheckError();
            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }

    public void Run() throws GLException {
        Init();
        Loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    private void calculateDT() {
        double current_time = glfwGetTime();
        delta_time = current_time - last_time;
        last_time = current_time;

        if (delta_time > 0.1) {
            delta_time = 0.1;
        }
    }



    public static void main(String[] args) throws Exception {
        Engine.getInstance().Run();
    }

    public void onKeyPress() {
        if (glfwGetKey(window, GLFW_KEY_TAB) == GLFW_TRUE) {

            Scene main = SceneManager.getInstance().GetScene("main");

            for (int i = 0; i < 10; i++) {
                int s = 3;
                Random rand = new Random();

                Float x = (float) rand.nextInt(s);
                Float y = (float) rand.nextInt(s);
                Float z = (float) rand.nextInt(s);

                Float colorx = rand.nextFloat();
                Float colory = rand.nextFloat();
                Float colorz = rand.nextFloat();

                main.getModels().get(1).setModelMatrix(new Matrix4f().identity().translate(new Vector3f(x, y, z)));
                main.getLights().get(i).position = new Vector3f(x, y, z);
            }
        }

    }

    public static int getScreenWidth() {
        return getInstance().screenWidth;
    }

    public static int getScreenHeight() {
        return getInstance().screenHeight;
    }

    public static Engine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Engine();
        }

        return INSTANCE;
    }

    public Long getWindowLong() {
        return window;
    }
    public static Long getWindow() {
        return getInstance().getWindowLong();
    }
}
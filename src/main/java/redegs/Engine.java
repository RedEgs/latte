package redegs;

import imgui.ImGui;
import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import redegs.engine.engine.entities.Billboard;
import redegs.engine.engine.entities.ControllableCamera;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.imgui.UIManager;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.Scene;
import redegs.engine.graphics.Cubemap;
import redegs.engine.graphics.MeshPrimitives;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;
import redegs.engine.graphics.pipelines.DeferredPipeline;
import redegs.engine.graphics.system.Renderer;
import redegs.engine.util.GLException;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBFramebufferSRGB.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_OUTPUT_SYNCHRONOUS;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static redegs.engine.util.Debug.glCheckError;


public class Engine {
    // The window handle
    private static Engine INSTANCE;
    private static Long window;
    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();

    private final Float version = 0.2f;
    private final String name = "Latte" + " " + version;

    private final int screenWidth = 1280;
    private final int screenHeight = 720;

    private double last_time, delta_time;
    EntitySceneManager esm = EntitySceneManager.getInstance();
    UIManager uim;


    private void Init() {
        // Set up an error callback. The default implementation
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

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            assert vidmode != null;
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

        //input = Input.getInstance();
        glfwSetKeyCallback(Engine.getWindow(), (w, key, scancode, action, mods) -> {
            Engine.onKeyPress(key, scancode, action, mods);
        });

        GL.createCapabilities();
        glViewport(0, 0, screenWidth, screenHeight);
        glEnable(GL_DEPTH_TEST); // CRITICAL: Enable depth testing!
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glEnable(GL_FRAMEBUFFER_SRGB);
        glEnable(GL_CULL_FACE);

        uim =  UIManager.getInstance();

        UIContext context = new UIContext(){
            @Override
            public void Draw() {
                super.Draw();
                ImGui.text("Hello");
            }
        };

        uim.AddContext(context);

    }

    private void Loop() throws GLException {

        Renderer<DeferredPipeline> renderer = new Renderer<>(DeferredPipeline::new);
        esm.setRenderer(renderer);

        ControllableCamera camera = new ControllableCamera(screenWidth, screenHeight);
        camera.setPosition(new Vector3f(0, 0, 3f));

        int m_id, m_id2;
        Scene main = new Scene(camera) {};
        Model m = new Model("src/main/resources/scene.gltf");
        m.getModelMatrix().scale(.1f);
        m.getModelMatrix().rotate((float) Math.toRadians(-90), new Vector3f(1, 0, 0));
        m.centerOrigin();
        //m.getTransform().position.set(0, 0, 0);

        m_id = esm.createEntity(m);
        esm.createEntity(new DirectionalLightSource(new Vector3f(0, -10, 0), new Vector3f(.05f), new Vector3f(.5f), new Vector3f(.3f)));
//
        Model xm = Model.fromMesh(MeshPrimitives.cube());
        xm.getTransform().model_matrix.translate(0, -1, 0);
        esm.createEntity(xm);

        Cubemap g = Cubemap.fromFile("src/main/resources/skybox");
        esm.createEntity(g);



        for (int i = 0; i < 10; i++) {
            int s = 3;
            Random rand = new Random();

            float x = (float) rand.nextInt(s);
            float y = (float) rand.nextInt(s);
            float z = (float) rand.nextInt(s);

            float colorx = rand.nextFloat();
            float colory = rand.nextFloat();
            float colorz = rand.nextFloat();


            //esm.getComponent(m_id, Model.class).getModelMatrix().translate(new Vector3f(x, y, z));
            esm.createEntity(new PointLightSource(new Vector3f(x, y, z), new Vector3f(colorx, colory, colorz), 10f, 3f));
            Billboard b = new Billboard("src/main/resources/icons/point-light.png");
            esm.createEntity(b);
            b.setPosition(new Vector3f(x, y, z));
        }


        esm.AddScene(main, "main");







        glCheckError();

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

            calculateDT();
            esm.Execute(delta_time, glfwGetTime());
            uim.Execute();

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

    public static void onKeyPress(int key, int scancode, int action, int mods) {
        EntitySceneManager esm = EntitySceneManager.getInstance();

        esm.GetScene().camera.onKeyPress(key, scancode, action, mods);

        if (action == GLFW_PRESS) {
            if (key == GLFW_KEY_TAB) {
                esm.GetScene().camera.toggleMouseLock();
            }
        } else {

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

    public static ImGuiImplGlfw getGlfw() {
        return getInstance().glfw;
    }

    public static ImGuiImplGl3 getGl3() {
        return getInstance().gl3;
    }

    public Long getWindowLong() {
        return window;
    }
    public static Long getWindow() {
        return getInstance().getWindowLong();
    }
}
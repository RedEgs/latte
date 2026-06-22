package redegs;

import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import redegs.engine.engine.components.Billboard;
import redegs.engine.engine.components.ControllableCamera;
import redegs.engine.engine.events.KeyPressEvent;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.imgui.UIManager;
import redegs.engine.engine.imgui.context.EditorUIContext;
import redegs.engine.engine.system.*;
import redegs.engine.engine.system.component.ComponentBootstrapper;
import redegs.engine.engine.system.scene.Scene;
import redegs.engine.engine.system.scene.SceneLoader;
import redegs.engine.graphics.*;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;
import redegs.engine.graphics.pipelines.DeferredPipeline;
import redegs.engine.graphics.system.render.Renderer;
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

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private double last_time, delta_time;
    EntitySceneManager esm = EntitySceneManager.getInstance();
    UIManager uim;
    static Renderer<DeferredPipeline> renderer ;
    SceneLoader loader = new SceneLoader();


    private void Init() {
        ComponentBootstrapper.scanAndRegister();
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
            onKeyPress( new KeyPressEvent(key, scancode, action, mods));
        });
        glfwSetFramebufferSizeCallback(Engine.getWindow(), (w, width, height) -> {
            this.screenWidth = width; this.screenHeight = height;
            renderer.resize(width, height);
            glViewport(0,0,width,height);
        });

        GL.createCapabilities();
        glViewport(0, 0, screenWidth, screenHeight);
        glEnable(GL_DEPTH_TEST); // CRITICAL: Enable depth testing!
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glEnable(GL_FRAMEBUFFER_SRGB);
        glEnable(GL_CULL_FACE);

        loader.register(Transform.class);
        loader.register(Cubemap.class);
        loader.register(DirectionalLightSource.class);
        loader.register(ControllableCamera.class);
        loader.register(Camera.class);
        loader.register(Texture.class);
        loader.register(Model.class);
        loader.register(PointLightSource.class);


        uim =  UIManager.getInstance();
        uim.AddContext(new EditorUIContext());

    }


    private void Loop() throws GLException {
        renderer= new Renderer<>(DeferredPipeline::new);
        esm.setRenderer(renderer);

        Scene main = new Scene() {};
        esm.AddScene(main, "main");

        renderer.BuildPipeline();

        int camera_id = esm.createEntity();
        ControllableCamera camera = new ControllableCamera(screenWidth, screenHeight, camera_id);
        camera.setPosition(new Vector3f(0, 0, 3f));
        main.addComponent(camera_id, camera);

//        int m_id = esm.createEntity();
//        Model m = new Model(m_id, "src/main/resources/scene.gltf");
//        main.addComponent(m_id, m);
//        m.getTransform().scale = new Vector3f(.1f);
//        m.getTransform().rotation = new Vector3f(-90, 0, 0);
//        m.centerOrigin();
//
//        m.getTransform().position.set(0, 0, 0);
//
//        int d_id = main.createEntity();
//        var d = new DirectionalLightSource(new Vector3f(0, -10, 0), new Vector3f(.05f), new Vector3f(.5f), new Vector3f(.3f), d_id);
//        main.addComponent(d_id, d);

//        int c_id = main.createEntity();
//        var c = Cubemap.fromFile("src/main/resources/skybox", c_id);
//        main.addComponent(c_id, c);


//        for (int i = 0; i < 1; i++) {
//            int s = 3;
//            Random rand = new Random();
//
//            float x = (float) rand.nextInt(s);
//            float y = (float) rand.nextInt(s);
//            float z = (float) rand.nextInt(s);
//
//            float colorx = rand.nextFloat();
//            float colory = rand.nextFloat();
//            float colorz = rand.nextFloat();
//
//
//            //esm.getComponent(m_id, Model.class).getModelMatrix().translate(new Vector3f(x, y, z));
//            int id = main.createEntity();
////            var t = new Transform(id);
//            var p = new PointLightSource(new Vector3f(x, y, z), new Vector3f(colorx, colory, colorz), 10f, 3f, id);
//            var b = new Billboard("src/main/resources/icons/point-light.png", id);
////            esm.addComponent(id, t);
//            esm.addComponent(id, p);
//            esm.addComponent(id, b);
//
//        }









        glCheckError();
        UIManager.getInstance().ReadyUp();

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

            calculateDT();
            esm.Execute(delta_time, glfwGetTime());


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

    public static void onKeyPress(KeyPressEvent event) {
        EntitySceneManager esm = EntitySceneManager.getInstance();
        if (esm.GetScene().camera != null) {
            esm.GetScene().camera.onKeyPress(event);
            for (UIContext context: getUIManager().getUIContexts()) {
                context.onKeyPress(event);
            }

            if (event.action == GLFW_PRESS) {
                if (event.key == GLFW_KEY_F1 ) {
                    renderer.toggleDebugRendering();
                }
                else if (event.key == GLFW_KEY_F2) {
                    getUIManager().getEditorContext().hidden = !getUIManager().getEditorContext().hidden;
                }
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

    public static double getDeltaTime() {
        return getInstance().delta_time;
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
    public static UIManager getUIManager() {
        return getInstance().uim;
    }
    public static SceneLoader getSceneLoader() {
        return getInstance().loader;
    }

}
package redegs.engine.engine.system;

import imgui.ImGui;
import imgui.ImVec2;
import org.lwjgl.glfw.GLFW;

public class PerformanceMetricManager {
    private static PerformanceMetricManager INSTANCE;

    private double lastTime = 0;
    private int frameCount = 0;
    private int fps = 0;

    private static final int FRAMETIME_HISTORY_SIZE = 100;
    private final float[] frametimeHistory = new float[FRAMETIME_HISTORY_SIZE];
    private int frametimeOffset = 0;
    private double lastFrameTime = 0;

    public static PerformanceMetricManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PerformanceMetricManager();
        }
        return INSTANCE;
    }

    public int getFps() {
        double currentTime = GLFW.glfwGetTime();
        frameCount++;

        // Track frametime
        double frametime = (currentTime - lastFrameTime) * 1000.0; // convert to ms
        frametimeHistory[frametimeOffset] = (float) frametime;
        frametimeOffset = (frametimeOffset + 1) % FRAMETIME_HISTORY_SIZE;
        lastFrameTime = currentTime;

        if (currentTime - lastTime >= 1.0) {
            fps = frameCount;
            frameCount = 0;
            lastTime = currentTime;
        }

        return fps;
    }

    public void renderFrametimeGraph() {
        ImGui.begin("Performance");

        ImGui.text("FPS: " + fps);

        // Find max frametime for dynamic scaling
        float max = 0;
        for (float f : frametimeHistory) {
            if (f > max) max = f;
        }

        ImGui.plotLines(
                "##frametime",          // label (## hides it)
                frametimeHistory,       // float[] of values
                frametimeOffset        // offset into the circular buffer
                // overlay text shown on the graph
                // min scale
                // max scale (floor at 33.3ms = 30fps)
                // graph size (0 width = fill available)
        );

        ImGui.text(String.format("%.2f ms", frametimeHistory[(frametimeOffset - 1 + FRAMETIME_HISTORY_SIZE) % FRAMETIME_HISTORY_SIZE]));

        ImGui.end();
    }
}
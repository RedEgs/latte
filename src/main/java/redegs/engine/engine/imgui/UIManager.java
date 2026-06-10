package redegs.engine.engine.imgui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import redegs.Engine;


import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.ARBFramebufferSRGB.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

public final class UIManager {
    private static UIManager INSTANCE;

    private final List<UIContext> contexts = new ArrayList<>();


    public UIManager() {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(Engine.getScreenWidth(), Engine.getScreenHeight());
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        Engine.getGlfw().init(Engine.getWindow(), true);
        Engine.getGl3().init("#version 330");
    }

    public void Execute() {
        glDisable(GL_FRAMEBUFFER_SRGB);

        Engine.getGlfw().newFrame();
        Engine.getGl3().newFrame();
        ImGui.newFrame();
        DrawUIs();
        ImGui.render();
        Engine.getGl3().renderDrawData(ImGui.getDrawData());

        glEnable(GL_FRAMEBUFFER_SRGB);

    }

    private void DrawUIs() {
        for (UIContext context : contexts) {
            context.Draw();
        }
    }

    public void AddContext(UIContext context) {
        this.contexts.add(context);
    }




    public static UIManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UIManager();
        }

        return INSTANCE;
    }
}

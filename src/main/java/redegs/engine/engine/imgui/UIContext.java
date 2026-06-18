package redegs.engine.engine.imgui;

import imgui.ImGui;
import imgui.ImGuiIO;
import redegs.engine.engine.events.KeyPressEvent;

public class UIContext {
    public static final ImGuiIO io = ImGui.getIO();

    public UIContext() {}
    public void Draw() {}
    public void onKeyPress(KeyPressEvent event) {

    }

}

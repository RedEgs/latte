package redegs.engine.engine.imgui.context;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;

import java.util.List;

public class EditorUIContext extends UIContext {
    EntitySceneManager esm = EntitySceneManager.getInstance();
    public int selected_entity = -1;
    public EditorUIContext() {}

    @Override
    public void Draw() {
        super.Draw();

        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Create")) {
                }
                if (ImGui.menuItem("Open", "Ctrl+O")) {
                }
                if (ImGui.menuItem("Save", "Ctrl+S")) {
                }
                if (ImGui.menuItem("Save as..")) {
                }
                if (ImGui.menuItem("Window")) {

                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }

        if (ImGui.begin("Scene Hierarchy", ImGuiWindowFlags.NoBackground)) {
            if (ImGui.treeNode("Scene Hierarchy")) {
                for (int i = 0; i < esm.GetScene().getEntities(); i++) { // Iterate over all the stores
                    if (ImGui.selectable("Entity " + String.valueOf(i))) {
                        selected_entity = i;
                    }
                }
                ImGui.treePop();
            }
            ImGui.end();
        }

        if (ImGui.begin("Inspector", ImGuiWindowFlags.NoBackground)) {
            if (selected_entity != -1) {
                if (ImGui.treeNode("Inspector")) {
                    System.out.println("Hello");
                    List<?> components = esm.GetScene().getComponents(selected_entity);
                    for (var c : components) {
                        ImGui.selectable(c.toString());
                    }
                    ImGui.treePop();
                }
            }
            ImGui.end();
        }

    }
}

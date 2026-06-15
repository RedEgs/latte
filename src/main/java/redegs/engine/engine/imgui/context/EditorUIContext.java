package redegs.engine.engine.imgui.context;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;

import java.util.HashMap;
import java.util.List;

public class EditorUIContext extends UIContext {
    EntitySceneManager esm = EntitySceneManager.getInstance();


    private int selected_entity = -1;
    private HashMap<Integer, ImBoolean> selected_entities = new HashMap<>();
    private ImBoolean display_empty_entities = new ImBoolean(false);

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

            ImGui.checkbox("Display Empty Entities", display_empty_entities);
            ImGui.spacing();
            ImGui.separator();
            ImGui.indent(16.0f);

            if (ImGui.collapsingHeader("Scene Hierarchy")) {
                ImGui.indent(16.0f);
                for (int i = 0; i < esm.GetScene().getEntities(); i++) { // Iterate over all the stores
                    if (esm.GetScene().getComponents(i).isEmpty() && !display_empty_entities.get()) continue;

                    if (selected_entities.get(i) == null) {
                        selected_entities.put(i, new ImBoolean(false));
                    }

                    if (ImGui.selectable("Entity " + String.valueOf(i), selected_entities.get(i))) {
                        if (selected_entity != -1) {
                            List<?> components = esm.GetScene().getComponents(selected_entity);
                            if (!components.isEmpty()) {
                                for (var c : components) {
                                    try{
                                        Component r = (Component) c;
                                        r.OnEditorDeselect();


                                    } catch (Exception e) {
                                        if (e.equals(new ClassCastException())) {
                                            System.err.println("Cannot cast component to Component");
                                        } else {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        }

                        selected_entity = i;

                        List<?> components = esm.GetScene().getComponents(selected_entity);
                        if (!components.isEmpty()) {
                            for (var c : components) {
                                try{
                                    Component r = (Component) c;
                                    r.OnEditorSelect();


                                } catch (Exception e) {
                                    if (e.equals(new ClassCastException())) {
                                        System.err.println("Cannot cast component to Component");
                                    } else {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }

                        for (Integer x: selected_entities.keySet()) {
                            if (x == i) continue;

                            selected_entities.get(x).set(false);
                        }
                    }

                }

            }
            ImGui.end();
        }

        if (ImGui.begin("Inspector", ImGuiWindowFlags.NoBackground)) {



            if (selected_entity != -1) {
                ImGui.text("Entity " + selected_entity);
            } else {
                ImGui.text("No entity selected.");
            }

            ImGui.separator();

            ImGui.indent(16.0f);
            if (ImGui.collapsingHeader("Inspector")) {
                if (selected_entity != -1) {



                    List<?> components = esm.GetScene().getComponents(selected_entity);
                    if (!components.isEmpty()) {
                        for (var c : components) {
                            try{
                                Component r = (Component) c;

                                ImGui.indent(16.0f);
                                if (ImGui.collapsingHeader(r.getName())) {
                                    r.OnEditorInspect();
                                };
                                ImGui.unindent(16.0f);
                                ImGui.spacing();


                            } catch (Exception e) {
                                if (e.equals(new ClassCastException())) {
                                    System.err.println("Cannot cast component to Component");
                                } else {
                                    throw new RuntimeException(e);
                                }


                            }

                        }
                    } else {
                        ImGui.text("Entity has no components.");
                    }
                }

            }
            ImGui.end();
        }

    }
}

package redegs.engine.engine.imgui.context;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import redegs.Engine;
import redegs.engine.engine.events.KeyPressEvent;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.imgui.UIManager;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.PerformanceMetricManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentRegistry;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.Transform;

import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class EditorUIContext extends UIContext {
    EntitySceneManager esm = EntitySceneManager.getInstance();

    public boolean hidden = false;

    private int selected_entity = -1;
    private HashMap<Integer, ImBoolean> selected_entities = new HashMap<>();
    private ImBoolean display_empty_entities = new ImBoolean(false);

    protected int currentMode = Mode.LOCAL;
    protected int currentOperation = Operation.TRANSLATE;

    protected ImGuiTextFilter componentTextFiler = new ImGuiTextFilter();

    public EditorUIContext() {}

    @Override
    public void onKeyPress(KeyPressEvent event) {
        super.onKeyPress(event);
        if (event.action == GLFW_PRESS) {
            if (event.key == GLFW_KEY_G) {
                currentOperation = Operation.TRANSLATE;
            } else if (event.key == GLFW_KEY_R) {
                currentOperation = Operation.ROTATE;
            } else if (event.key == GLFW_KEY_T) {
                currentOperation = Operation.SCALE;
            }
        }

    }

    @Override
    public void Draw() {
        super.Draw();
        if (hidden)
            return;;

        PerformanceMetricManager pfm =  PerformanceMetricManager.getInstance();

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

        if (ImGui.begin("Scene Hierarchy")) {

            ImGui.checkbox("Display Empty Entities", display_empty_entities);
            ImGui.spacing();
            ImGui.separator();
            ImGui.indent(16.0f);

            if (ImGui.collapsingHeader("Scene Hierarchy")) {
                if (ImGui.beginPopupContextWindow()) {
                    if (ImGui.button("Create Entity")) {
                        selected_entity = esm.createEntity();
                        selected_entities.put(selected_entity, new ImBoolean(true));

                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }

                ImGui.indent(16.0f);

                for (int i = 0; i < esm.GetScene().getEntities(); i++) {
                    // If entity has no components and we don't want to display them.
                    if (esm.GetScene().getComponents(i).isEmpty() && !display_empty_entities.get() && selected_entity != i) continue;

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
                    if (ImGui.beginPopupContextItem()) {
                        if (ImGui.button("Delete Entity")) {
                            esm.deleteEntity(i);
                        }

                        ImGui.endPopup();
                    }

                }



            }
            ImGui.end();
        }

        if (ImGui.begin("Inspector")) {
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

                                    if (c instanceof Transform) {
                                        Transform m = (Transform) c;
                                        float[] viewMatrix = new float[16];
                                        float[] projMatrix = new float[16];
                                        float[] modelMatrix = new float[16];

                                        esm.GetScene().camera.getView().get(viewMatrix);
                                        esm.GetScene().camera.getProjection().get(projMatrix);
                                        m.model_matrix.get(modelMatrix);

                                        EditorUIContext ui = UIManager.getInstance().getEditorContext();
                                        ImGuizmo.manipulate(viewMatrix, projMatrix, currentOperation, currentMode, modelMatrix);

                                        if (ImGuizmo.isUsing()) {
                                            float[] translation = new float[3];
                                            float[] rotation = new float[3];
                                            float[] scale = new float[3];
                                            ImGuizmo.decomposeMatrixToComponents(modelMatrix, translation, rotation, scale);

                                            // Apply back to the model
                                            m.position.set(translation[0], translation[1], translation[2]);
                                            m.rotation.set(rotation[0], rotation[1], rotation[2]);
                                            m.scale.set(scale[0], scale[1], scale[2]);
                                            m.updateModelMatrix(); // force rebuild of the matrix from transform

                                        }
                                    }
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

                    ImGui.spacing();
                    ImGui.separator();
                    ImGui.spacing();
                    if (ImGui.button("New Component", ImGui.getContentRegionAvailX(), 35)) {
                        ImGui.openPopup("NewComponentPopup");
                    }
                    if (ImGui.beginPopup("NewComponentPopup")) {
                        for (String category : ComponentRegistry.categories()) {
                            if (ImGui.beginMenu(category)) {
                                for (ComponentRegistry.Entry entry : ComponentRegistry.byCategory(category)) {
                                    if (ImGui.menuItem(entry.name())) {
                                        Component c = entry.factory().get();
                                        esm.addComponent(selected_entity, c);
                                    }
                                    if (ImGui.isItemHovered()) {
                                        ImGui.setTooltip(entry.description());
                                    }
                                }
                                ImGui.endMenu();
                            }
                        }
                        ImGui.endPopup();
                    }
                }

            }
            ImGui.end();
        }

        ImGui.setNextWindowSize(200, 60);
        ImGui.setNextWindowPos(Engine.getScreenWidth() - 220, 40);
        if (ImGui.begin("Performance Metrics")) {
            ImGui.text("FPS: " + pfm.getFps());


            ImGui.end();
        }

    }

    public int getGizmoCurrentMode() {
        return this.currentMode;
    }

    public int getGizmoCurrentOperation() {
        return this.currentOperation;
    }
}

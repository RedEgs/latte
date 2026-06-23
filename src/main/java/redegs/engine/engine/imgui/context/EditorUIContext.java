package redegs.engine.engine.imgui.context;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.ImVec4;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorLanguage;
import imgui.extension.texteditor.flag.TextEditorColor;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.luaj.vm2.LuaError;
import redegs.Engine;
import redegs.engine.engine.events.KeyPressEvent;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.imgui.UIManager;
import redegs.engine.engine.script.Script;
import redegs.engine.engine.script.ScriptAPI;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.FileDialogs;
import redegs.engine.engine.system.PerformanceMetricManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentRegistry;
import redegs.engine.engine.system.scene.Scene;
import redegs.engine.engine.system.scene.SceneLoader;
import redegs.engine.graphics.Transform;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class EditorUIContext extends UIContext {
    EntitySceneManager esm = EntitySceneManager.getInstance();
    public boolean hidden = false;

    private int selected_entity = -1;
    private HashMap<Integer, HashMap<String, ImBoolean>> selected_entities = new HashMap<>();
    private ImBoolean display_empty_entities = new ImBoolean(false);

    protected int currentMode = Mode.LOCAL;
    protected int currentOperation = Operation.TRANSLATE;

    protected ImString filenameSaveAs = new ImString();
    protected boolean openSaveAs = false;

    protected ImGuiTextFilter componentTextFiler = new ImGuiTextFilter();

    protected HashMap<Script, TextEditor> scriptEditors = new HashMap<>();


    public EditorUIContext() {}

    ImBoolean getEntitySelectedRef(int id) {
        return selected_entities.get(id).get("Selected");
    }
    ImBoolean getEntityDeletedRef(int id) {
        return selected_entities.get(id).get("Deleted");
    }

    Boolean isEntitySelected(int id) {
        return selected_entities.get(id).get("Selected").get();
    }

    Boolean isEntityDeleted(int id) {
        return selected_entities.get(id).get("Deleted").get();
    }

    void setEntitySelected(int id, boolean val) {
        selected_entities.get(id).get("Selected").set(val);
    }

    void setEntityDeleted(int id, boolean val) {
        selected_entities.get(id).get("Deleted").set(val);
    }


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
                if (ImGui.beginMenu("Create...")) {
                    if (ImGui.menuItem("New Scene")) {
                        Scene scene = new Scene();
                        EntitySceneManager.getInstance().AddScene(scene, "new");
                        EntitySceneManager.getInstance().SetScene("new");
                    }
                    ImGui.endMenu();
                }
                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    try {
                        String path = FileDialogs.openFile("Select Scene", FileDialogs.homeDirectory(), new String[]{"*.json"}, "Select .json");
                        Scene scene = Engine.getSceneLoader().LoadScene(path);
                        EntitySceneManager.getInstance().AddScene(scene, path);
                        EntitySceneManager.getInstance().SetScene(path);

                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
                if (ImGui.menuItem("Open Recent")) {
                    try {
                        Scene scene = Engine.getSceneLoader().LoadScene("recent.json");
                        EntitySceneManager.getInstance().AddScene(scene, "recent");
                        EntitySceneManager.getInstance().SetScene("recent");
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
                if (ImGui.menuItem("Save", "Ctrl+S")) {
                    try {
                        Engine.getSceneLoader().SaveScene(esm.GetScene(), "recent.json");
                    } catch (Exception e) {
                        System.err.println(e);
                    }

                }
                if (ImGui.menuItem("Save as..")) {
                    String loc = FileDialogs.saveFile("Save scene...", FileDialogs.homeDirectory(), new String[]{"*.json"}, "Scene Location");
                    try {
                        Engine.getSceneLoader().SaveScene(EntitySceneManager.getInstance().GetScene(), loc);
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                }

                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Test")) {
               if (ImGui.menuItem("Play")) {
                   try {
                       Engine.getSceneLoader().SaveScene(EntitySceneManager.getInstance().GetScene(), "recent_test.json");
                       Engine.play(false, "recent_test.json");
                   } catch (Exception e) {
                       System.err.println("Couldn't launch in play mode");
                   }
               }
               if (Engine.getGameMode()) {
                   if (ImGui.menuItem("Stop Testing")) {
                       Engine.setGameMode(false);
                   }
               } else {
                   if (ImGui.menuItem("Start Testing")) {
                       Engine.setGameMode(true);
                   }
               }


               ImGui.endMenu();
            }
            if (Engine.getGameMode()) {
                if (ImGui.menuItem("Stop Testing")) {
                    Engine.setGameMode(false);
                }
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
                        HashMap hm = new HashMap<>();
                        hm.put("Selected", new ImBoolean(true));
                        hm.put("Deleted", new ImBoolean(false));

                        selected_entity = esm.createEntity();
                        selected_entities.put(selected_entity, hm);

                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }

                ImGui.indent(16.0f);

                for (int i = 0; i < esm.GetScene().getEntities(); i++) {
                    // If entity has no components and we don't want to display them.
                    if (esm.GetScene().getComponents(i).isEmpty() && !display_empty_entities.get() && selected_entity != i) continue;
                    if (selected_entities.get(i) == null) {
                        HashMap hm = new HashMap<>();
                        hm.put("Selected", new ImBoolean(false));
                        hm.put("Deleted", new ImBoolean(false));
                        selected_entities.put(i, hm);

                    }
                    if (isEntityDeleted(i)) continue;

                    if (ImGui.selectable("Entity " + i, getEntitySelectedRef(i))) {
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
                                    if (c instanceof Script s) {
                                        if (scriptEditors.get(s) == null)
                                            scriptEditors.put(s, new TextEditor());

                                        TextEditor editor = scriptEditors.get(s);
                                        editor.setText(s.getSource());
                                    }


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
                            setEntitySelected(x, false);
                        }
                    }
                    if (ImGui.beginPopupContextItem()) {
                        if (ImGui.button("Delete Entity")) {
                            esm.deleteEntity(i);
                            setEntityDeleted(i, true);
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
                                    else if (c instanceof Script s) {
                                        if (scriptEditors.get(s) == null)
                                            scriptEditors.put(s, new TextEditor());
                                        TextEditor editor = scriptEditors.get(s);
                                        editor.setLanguage(TextEditorLanguage.Lua());
                                        editor.setPaletteColor(TextEditorColor.currentLineNumber, 0xFFFFC080);

                                        if (ImGui.button("Load")) {
                                            if (s.isLoaded())
                                                editor.setText(s.getSource());
                                        } ImGui.sameLine();
                                        if (ImGui.button("Save")) {
                                            try {
                                                Files.writeString(Paths.get(s.getLocation()), editor.getText(), StandardCharsets.UTF_8);
                                            } catch (Exception e) {
                                                throw new RuntimeException("Failed to save script editor to lua file path: " + s.getLocation());
                                            }
                                        } ImGui.sameLine();
                                        if (ImGui.button("Rebuild")) {
                                            LuaError e = s.rebuild();
                                            if (e != null) {
                                                Integer ln = ScriptAPI.getErrorLine(e);
                                                if (ln != null)
                                                    editor.addMarker(ln, 0xff0000, 0xff0000, e.getMessage());
                                            } else {
                                                editor.clearMarkers();
                                            }
                                        } ImGui.sameLine();
                                        if (ImGui.button("Save & Rebuild")) {
                                            try {
                                                Files.writeString(Paths.get(s.getLocation()), editor.getText(), StandardCharsets.UTF_8);
                                            } catch (Exception e) {
                                                throw new RuntimeException("Failed to save script editor to lua file path: " + s.getLocation());
                                            }
                                            s.rebuild();
                                        }

                                        editor.render(s.getScriptName());
                                        ImGui.separator();
                                        ImGui.spacing();

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
                                        Component c = entry.factory().apply(selected_entity);
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

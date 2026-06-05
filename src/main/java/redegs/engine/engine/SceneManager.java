package redegs.engine.engine;

import redegs.engine.graphics.Model;
import redegs.engine.graphics.Renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SceneManager {
    private static SceneManager INSTANCE;

    private HashMap<String, Scene> scenes = new HashMap<String, Scene>();
    private Scene current_scene;
    private String current_scene_name;

    private Renderer current_renderer;

    public SceneManager() {}

    public void Execute(double delta_time, double elapsed_time) {
        if (current_scene != null) {
            UpdateScene(delta_time, elapsed_time);
        } else {
            System.err.println("No scenes exist.");
        }

        if (current_renderer != null) {
            current_renderer.Execute(delta_time, elapsed_time);
        } else {
            System.err.println("No renderer has been set.");
        }
    }

    private void UpdateScene(double delta_time, double elapsed_time) {
        if (current_renderer.getModels().equals(current_scene.getModels())) {
            List<Model> diff = new ArrayList<>(current_scene.getModels());
            diff.removeAll(current_renderer.getModels());
            current_renderer.SubmitModels(diff);
        }


        current_scene.Update(delta_time, elapsed_time);
        current_renderer.setCamera(current_scene.getCamera());
    }

    public void AddScene(Scene scene, String name) {
        if (scenes.get(name) == null) {
            scenes.put(name, scene);

            if (current_scene == null) {
                SetScene(scene, name);
            }
        }
    }

    public void SetScene(String name) {
        Scene s = scenes.get(name);
        if (s != null) {
            SetScene(s, name);
        }
    }

    private void SetScene(Scene scene, String name) {
        if (scene != this.current_scene) {
            current_renderer.ClearModels();
        }

        this.current_scene = scene;
        this.current_scene_name = name;

        current_renderer.SubmitModels(current_scene.getModels());
    }

    public Scene GetScene(String name) {
        return this.scenes.get(name);
    }
    public Scene GetScene() { return this.current_scene; }
    public String GetSceneName() {
        return this.current_scene_name;
    }


    public void setRenderer(Renderer renderer) {
        this.current_renderer = renderer;
    }

    public Renderer getRenderer() {
        return this.current_renderer;
    }

    public static SceneManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SceneManager();
        }
        return INSTANCE;
    }
}

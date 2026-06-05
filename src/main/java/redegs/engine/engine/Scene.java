package redegs.engine.engine;

import redegs.Engine;
import redegs.engine.graphics.Model;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Camera camera;
    private final List<Model> models;

    public Scene(Camera camera) {
        models = new ArrayList<>();
        this.camera = camera;
    }

    public Scene() {
        models = new ArrayList<>();
        camera = new Camera(Engine.getScreenWidth(), Engine.getScreenHeight());
    }

    public void Update(double delta_time, double elapsed_time) {
        if (camera != null) {
            camera.Update(delta_time, elapsed_time);
        } else {
            System.err.println("No camera has been set for current scene.");
        }
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public List<Model> getModels() {
        return models;
    }

    public <T extends Camera> void setCamera(T camera) {
        this.camera = camera;
    }
    public Camera getCamera() {
        return camera;
    }
}

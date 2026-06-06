package redegs.engine.engine.system;

import redegs.Engine;
import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    public Camera camera;
    public EntitySceneManager esm = EntitySceneManager.getInstance();

    public Scene(Camera camera) {
        this.camera = camera;
        Init();
    }

    public Scene() {
        camera = new Camera(Engine.getScreenWidth(), Engine.getScreenHeight());
        Init();
    }

    public void Update(double delta_time, double elapsed_time) {
        if (camera != null) {
            camera.Update(delta_time, elapsed_time);
        } else {
            System.err.println("No camera has been set for current scene.");
        }
        OnUpdate();
    }

    private void Init() {}
    private void OnUpdate() {}

    public <T extends Camera> void setCamera(T camera) {
        this.camera = camera;
    }
    public Camera getCamera() {
        return camera;
    }
}

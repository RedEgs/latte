package redegs.engine.engine.system.scene;

import java.util.HashMap;
import java.util.Map;

public final class SceneRegistry {
    private final Map<String, Scene> scenes = new HashMap<>();
    private Scene currentScene;
    private String currentSceneName = "";

    public void addScene(String name, Scene scene) {
        scenes.putIfAbsent(name, scene);
        if (currentScene == null) {
            currentScene = scene;
            currentSceneName = name;
        }
    }
    public boolean setCurrentScene(String name) {
        Scene s = scenes.get(name);
        if (s == null) return false;
        currentScene = s;
        currentSceneName = name;
        return true;
    }

    public Scene getScene(String name) { return scenes.get(name); }
    public Scene getCurrentScene() { return currentScene; }
    public String getCurrentSceneName() { return currentSceneName; }
}
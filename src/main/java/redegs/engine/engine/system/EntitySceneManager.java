package redegs.engine.engine.system;

import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;
import redegs.engine.graphics.system.Renderer;

import java.util.HashMap;

public final class EntitySceneManager {
    private static EntitySceneManager INSTANCE;

    private final HashMap<String, Scene> scenes = new HashMap<String, Scene>();
    private final HashMap<Class<?>, ComponentStore<?>> stores = new HashMap<>();

    private Scene current_scene;
    private String current_scene_name;

    private int next_entity = 0;

    private Renderer current_renderer;

    public EntitySceneManager() {}

    public int createEntity() {
        return next_entity++;
    }

    @SafeVarargs
    public final <T> int createEntity(T... component) {
        int i = next_entity++;
        for (T c : component) {
            addComponent(i, c);
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    private <T> ComponentStore<T> getStore(Class<T> type) {
        return (ComponentStore<T>) stores.computeIfAbsent(
                type,
                k -> new ComponentStore<>()
        );
    }

    public <T> void addComponent(int entity, T component) {
        getStore((Class<T>) component.getClass()).add(entity, component);
    }

    public <T> T getComponent(int entity, Class<T> type) {
        return getStore(type).get(entity);
    }

    public <T> boolean hasComponent(int entity, Class<T> type) {
        return getStore(type).has(entity);
    }

    public <T> void removeComponent(int entity, Class<T> type) {
        getStore(type).remove(entity);
    }


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
        current_renderer.ClearModels();
        current_renderer.ClearLights();
        current_renderer.SubmitDirectionalLight(getStore(DirectionalLightSource.class).toList().get(0));

        current_renderer.SubmitLights(getStore(PointLightSource.class).toList());
        current_renderer.SubmitModels(getStore(Model.class).toList());

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
            current_renderer.ClearLights();
        }

        this.current_scene = scene;
        this.current_scene_name = name;

        current_renderer.SubmitLights(getStore(PointLightSource.class).toList());
        current_renderer.SubmitModels(getStore(Model.class).toList());
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

    public static EntitySceneManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntitySceneManager();
        }
        return INSTANCE;
    }
}

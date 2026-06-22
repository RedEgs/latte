package redegs.engine.engine.system;

import redegs.engine.engine.components.ControllableCamera;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentStore;
import redegs.engine.engine.system.component.PendingEntityStaging;
import redegs.engine.engine.system.scene.Scene;
import redegs.engine.engine.system.scene.SceneRegistry;
import redegs.engine.engine.system.scene.SceneRenderSync;
import redegs.engine.graphics.system.render.Renderer;

import java.util.*;
import java.util.function.Function;


public final class EntitySceneManager {
    private static EntitySceneManager INSTANCE;

    private final SceneRegistry sceneRegistry = new SceneRegistry();
    private final PendingEntityStaging staging = new PendingEntityStaging();

    private Renderer current_renderer;
    private SceneRenderSync renderSync;

    public EntitySceneManager() {}

    public static EntitySceneManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntitySceneManager();
        }
        return INSTANCE;
    }

    // ------------------------------------------------------------------
    // Frame execution
    // ------------------------------------------------------------------

    public void Execute(double delta_time, double elapsed_time) {
        Scene scene = sceneRegistry.getCurrentScene();

        if (scene != null) {
            scene.Update(delta_time, elapsed_time);
            if (renderSync != null) {
                renderSync.sync(scene);
            }
        } else {
            System.err.println("No scenes exist.");
        }

        if (current_renderer != null) {
            current_renderer.Execute(delta_time, elapsed_time);
        } else {
            System.err.println("No renderer has been set.");
        }
    }

    // ------------------------------------------------------------------
    // Scene management
    // ------------------------------------------------------------------

    public void AddScene(Scene scene, String name) {
        boolean wasEmpty = sceneRegistry.getCurrentScene() == null;
        sceneRegistry.addScene(name, scene);

        // If this is the first scene ever added, treat it as activation.
        if (wasEmpty && sceneRegistry.getCurrentScene() == scene) {
            activateScene(scene, name);
        }
    }

    public void SetScene(String name) {
        Scene scene = sceneRegistry.getScene(name);
        if (scene != null) {
            sceneRegistry.setCurrentScene(name);
            activateScene(scene, name);
        }
    }

    /** Shared logic for "a scene just became the active one". */
    private void activateScene(Scene scene, String name) {
        if (current_renderer != null) {
            if (scene.getStore(ControllableCamera.class).toList().size() < 0) {
                current_renderer.submitCamera(scene.getStore(ControllableCamera.class).toList().get(0));
            }
        }

        if (!staging.isEmpty()) {
            scene.mergeTemporaryStore(staging.getNextEntity(), staging.getStores());
            staging.clear();
        }

        if (renderSync != null) {
            renderSync.sync(scene);
        }
    }

    public Scene GetScene(String name) {
        return sceneRegistry.getScene(name);
    }

    public Scene GetScene() {
        return sceneRegistry.getCurrentScene();
    }

    public String GetSceneName() {
        return sceneRegistry.getCurrentSceneName();
    }

    // ------------------------------------------------------------------
    // Renderer
    // ------------------------------------------------------------------

    public void setRenderer(Renderer renderer) {
        this.current_renderer = renderer;
        this.renderSync = new SceneRenderSync(renderer);
    }

    public Renderer getRenderer() {
        return this.current_renderer;
    }

    // ------------------------------------------------------------------
    // Entity / component facade
    // ------------------------------------------------------------------

    public int createEntity() {
        Scene scene = sceneRegistry.getCurrentScene();
        return (scene != null) ? scene.createEntity() : staging.createEntity();
    }

    @SafeVarargs
    public final <T> int createEntity(T... components) {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            return scene.createEntity(components);
        }

        int entity = staging.createEntity();
        for (T c : components) {
            staging.addComponent(entity, c);
        }
        return entity;
    }

    public void deleteEntity(int entity) {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            List<?> comps = getComponents(entity);
            for (var comp : comps) {
                if (comp instanceof Component) {
                    Component c = (Component) comp;
                    c.OnDelete();
                }

                removeComponent(entity, comp.getClass());
            }

            return;
        }

        List<?> comps = staging.getComponents(entity);
        for (var comp : comps) {
            if (comp instanceof Component) {
                Component c = (Component) comp;
                c.OnDelete();
            }
            removeComponent(entity, comp.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void addComponent(int entity, T component) {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            scene.getStore((Class<T>) component.getClass()).add(entity, component);
        } else {
            staging.addComponent(entity, component);
        }
    }

    public <T> T getOrAddComponent(int entity, T component) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) component.getClass();
        if (hasComponent(entity, type)) {
            return getComponent(entity, type);
        }
        addComponent(entity, component);
        return component;
    }

    public <T> T getComponent(int entity, Class<T> type) {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            return scene.getStore(type).get(entity);
        }
        return staging.getStore(type).get(entity);
    }

    public <T> boolean hasComponent(int entity, Class<T> type) {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            return scene.getStore(type).has(entity);
        }
        return staging.getStore(type).has(entity);
    }

    public <T> void removeComponent(int entity, Class<T> type) {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            scene.getStore(type).remove(entity);
        } else {
            staging.getStore(type).remove(entity);
        }
    }


    public int getEntities() {
        Scene scene = sceneRegistry.getCurrentScene();
        return (scene != null) ? scene.getEntities() : staging.getNextEntity();
    }

    public List<?> getComponents(int entity) {
        List<Object> objects = new ArrayList<>();
        for (ComponentStore<?> cs : getStores().values()) {
            Object c = cs.get(entity);
            if (c != null) {
                objects.add(c);
            }
        }
        return objects;
    }

    public HashMap<Class<?>, ComponentStore<?>> getStores() {
        Scene scene = sceneRegistry.getCurrentScene();
        if (scene != null) {
            return scene.getStores();
        }
        return staging.getStores();
    }
}

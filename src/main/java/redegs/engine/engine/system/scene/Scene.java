package redegs.engine.engine.system.scene;

import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentStore;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.graphics.Camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scene {
    public Camera camera;

    private int next_entity = 0;

    public EntitySceneManager esm = EntitySceneManager.getInstance();
    private HashMap<Class<?>, ComponentStore<?>> stores = new HashMap<>();


    public Scene(Camera camera) {
        this.camera = camera;
        Init();
    }

    public Scene() {
        Init();
    }

    public void Update(double delta_time, double elapsed_time) {
        if (camera != null) {
            camera.OnUpdate();
        } else {
            System.err.println("(Scene.Java) No camera has been set for current scene.");
        }

        for (ComponentStore<?> store : stores.values()) {
            for (Object component : store.toList()) {
                if (component instanceof Component c) {
                    c.OnUpdate();
                }
            }
        }
        OnUpdate();
    }

    private void Init() {}
    private void OnUpdate() {}



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
    public <T> ComponentStore<T> getStore(Class<T> type) {
        return (ComponentStore<T>) getStores().computeIfAbsent(
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

    public int getEntities() {
        return next_entity;
    }

    public List<?> getComponents(int entity) {
        List<Object> objects = new ArrayList<>();
        for (ComponentStore<?> cs : getStores().values()) {
            var c = cs.get(entity);
            if (c != null) {
                objects.add(c);
            }
        }
        return objects;
    }

    public HashMap<Class<?>, ComponentStore<?>> getStores() {
        return stores;
    }

    public void mergeTemporaryStore(int entities, HashMap<Class<?>, ComponentStore<?>> componentStore) {
        this.stores = new HashMap<>(componentStore);
        this.next_entity = entities;
    }







    public <T extends Camera> void setCamera(T camera) {
        this.camera = camera;
    }
    public Camera getCamera() {
        return camera;
    }


}

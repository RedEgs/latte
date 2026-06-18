package redegs.engine.engine.system.component;

import redegs.engine.engine.system.EntitySceneManager;

public class Component {
    protected String name;
    protected int entity;

    public Component(int entity) {
        this.entity = entity;
        this.name = "GenericComponent";
    }

    public void OnUpdate() {}
    /** Check if the entity this component belongs to also has another component type. */
    protected <T> boolean entityHas(Class<T> type) {
        return EntitySceneManager.getInstance().hasComponent(entity, type);
    }

    /** Get a sibling component from the same entity. */
    protected <T> T entityGet(Class<T> type) {
        return EntitySceneManager.getInstance().getComponent(entity, type);
    }

    public void OnEditorInspect() {};

    public void OnEditorSelect() {}

    public void OnEditorDeselect() {}

    public void OnDelete() {}

    public String getName(){
        return name;
    };

    public int getEntity() {
        return this.entity;
    }

}

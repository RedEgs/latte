package redegs.engine.engine.system.component;

import com.google.gson.JsonObject;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.graphics.Shader;

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

    public void Draw(Shader shader) {}

    public JsonObject Save() {
        return new JsonObject();
    }

    /** Restore this component's state from JSON produced by Save(). Default: no-op. */
    public void Load(JsonObject data) {
        // no-op by default — components with no save-worthy state don't need to override
    }

    public String getName(){
        return name;
    };

    public int getEntity() {
        return this.entity;
    }

}

package redegs.engine.engine.system.component;

public class Component {
    protected String name;
    protected final int entity;

    public Component(int entity) {
        this.entity = entity;
        this.name = "GenericComponent";
    }

    public void OnEditorInspect() {};

    public void OnEditorSelect() {

    }

    public void OnEditorDeselect() {

    }

    public String getName(){
        return name;
    };

    public int getEntity() {
        return this.entity;
    }
}

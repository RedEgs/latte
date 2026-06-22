package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import org.joml.Vector3f;
import redegs.engine.engine.gson.Save;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;

public class LightSource extends Component {
    public Vector3f position;
    public Vector3f color;
    public float intensity;

    protected Transform transform;
    protected boolean _ownsTransform = false;

    private final float[] colorArr = new float[3];
    private final float[] intensitySlider = new float[1];

    public LightSource(Vector3f position, Vector3f color, float intensity, int entity) {
        super(entity);
        init(position, color, intensity);
    }

    public LightSource(Vector3f position, Vector3f color, float intensity) {
        super(EntitySceneManager.getInstance().createEntity());
        init(position, color, intensity);
    }

    private void init(Vector3f position, Vector3f color, float intensity) {
        name = "LightSourceComponent";

        this.position = position;
        this.color = color;
        this.intensity = intensity;

        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            _ownsTransform = false;

        } else {
            this.transform = new Transform(entity);
            EntitySceneManager.getInstance().addComponent(entity, this.transform);
            _ownsTransform = true;
        }
        this.transform.position.set(position);
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.add("position", Save.Vec3ToJson(position));
        o.add("color", Save.Vec3ToJson(color));
        o.addProperty("intensity", intensity);
        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);
        position = Save.JsonToVec3(data.getAsJsonObject("position"));
        color = Save.JsonToVec3(data.getAsJsonObject("color"));
        intensity = data.get("intensity").getAsFloat();
    }


    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        if (_ownsTransform) {
            transform.OnEditorInspect();
        }

        ImGui.spacing();
        ImGui.text("Lighting");
        ImGui.separator();
        ImGui.spacing();
        ImGui.indent(16.0f);

        // color: sync in, edit, sync back out
        colorArr[0] = color.x;
        colorArr[1] = color.y;
        colorArr[2] = color.z;
        if (ImGui.colorEdit3("Color", colorArr)) {
            color.set(colorArr[0], colorArr[1], colorArr[2]);
        }

        // intensity: sync in, edit, sync back out
        intensitySlider[0] = intensity;
        if (ImGui.sliderFloat("Intensity", intensitySlider, 0, 100)) {
            intensity = intensitySlider[0];
        }

        ImGui.unindent(16.0f);
    }

    public Transform getTransform() {
        return transform;
    }

    @Override
    public void OnUpdate() {
        super.OnUpdate();
        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            this._ownsTransform = false;
        }

        position.set(transform.position);
    }
}
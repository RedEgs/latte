package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.gson.Save;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;

@ComponentMeta(name = "Transform", category = "3D", description = "Manages position, rotation and scale of an entity.")
public class Transform extends Component {

    public Vector3f position = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public Vector3f scale = new Vector3f(1, 1, 1);

    private final float[] posArr = new float[3];
    private final float[] rotArr = new float[3];
    private final float[] scaleArr = new float[3];

    protected final Vector3f originOffset = new Vector3f(0, 0, 0);
    public Matrix4f model_matrix = new Matrix4f().identity();

    public Transform(int entity) {
        super(entity);
        this.name = "Transform";
    }

    static {

        ComponentRegistry.register(
                Transform.class,
                entity -> new Transform(entity)
        );
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.add("position", Save.Vec3ToJson(position));
        o.add("rotation", Save.Vec3ToJson(rotation));
        o.add("scale", Save.Vec3ToJson(scale));
        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);

        position = Save.JsonToVec3(data.getAsJsonObject("position"));
        rotation = Save.JsonToVec3(data.getAsJsonObject("rotation"));
        scale    = Save.JsonToVec3(data.getAsJsonObject("scale"));
        updateModelMatrix(); // rebuild derived state immediately after load
    }

    public void updateModelMatrix() {
        model_matrix.identity()
                .translate(position)
                .rotateXYZ(
                        (float) Math.toRadians(rotation.x),
                        (float) Math.toRadians(rotation.y),
                        (float) Math.toRadians(rotation.z)
                )
                .scale(scale)
                .translate(originOffset);
    }

    public void setPosition(Vector3f pos) {
        this.position.set(pos);
    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();
        ImGui.spacing();
        ImGui.text("Transform");
        ImGui.separator();
        ImGui.spacing();
        ImGui.indent(16.0f);

        // sync vec -> array
        posArr[0] = position.x;
        posArr[1] = position.y;
        posArr[2] = position.z;

        rotArr[0] = rotation.x;
        rotArr[1] = rotation.y;
        rotArr[2] = rotation.z;

        scaleArr[0] = scale.x;
        scaleArr[1] = scale.y;
        scaleArr[2] = scale.z;

        // edit via ImGui
        ImGui.inputFloat3("Position", posArr);
        ImGui.inputFloat3("Rotation", rotArr);
        ImGui.inputFloat3("Scale", scaleArr);

        // array -> vec
        position.set(posArr[0], posArr[1], posArr[2]);
        rotation.set(rotArr[0], rotArr[1], rotArr[2]);
        scale.set(scaleArr[0], scaleArr[1], scaleArr[2]);

        ImGui.unindent(16.0f);
    }
}
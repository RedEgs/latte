package redegs.engine.graphics;

import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.system.component.Component;

public class Transform extends Component {

    public Vector3f position = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public Vector3f scale = new Vector3f(1, 1, 1);

    private final float[] posArr = new float[3];
    private final float[] rotArr = new float[3];
    private final float[] scaleArr = new float[3];

    public Matrix4f model_matrix = new Matrix4f().identity();

    public Transform(int entity) {
        super(entity);
        this.name = "Transform";
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
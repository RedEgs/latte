package redegs.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.system.component.Component;

public class Transform extends Component {
    public Vector3f position = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public Vector3f scale = new Vector3f(1, 1, 1);

    public Matrix4f model_matrix = new Matrix4f().identity();

    public Transform(int entity) {
        super(entity);
        this.name = "Transform";

    }
}

package redegs.engine.graphics.lights;

import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.graphics.Transform;

@ComponentMeta(name = "Directional Light", category = "Lighting", description = "A directional light source.")
public class DirectionalLightSource extends Component {
    public Vector3f direction;
    public Vector3f ambient;
    public Vector3f diffuse;
    public Vector3f specular;

    protected Transform transform;
    private boolean _ownsTransform = false;

    private final float[] ambientArr = new float[3];
    private final float[] diffuseArr = new float[3];
    private final float[] specularArr = new float[3];

    public DirectionalLightSource(Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular, int entity) {
        super(entity);
        name = "DirectionalLightSourceComponent";

        this.direction = direction;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;

        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            _ownsTransform = false;
        } else {
            this.transform = new Transform(entity);
            _ownsTransform = true;
        }

        initRotationFromDirection();
    }

    public DirectionalLightSource(Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        super(EntitySceneManager.getInstance().createEntity());
        name = "DirectionalLightSourceComponent";

        this.direction = direction;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;

        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            _ownsTransform = false;
        } else {
            this.transform = new Transform(entity);
            _ownsTransform = true;
        }
        initRotationFromDirection();
    }

    /**
     * Derives an initial yaw/pitch (stored in transform.rotation.y / .x)
     * from the given direction vector, so the inspector starts in sync.
     */
    private void initRotationFromDirection() {
        Vector3f d = new Vector3f(direction).normalize();

        float pitch = (float) Math.toDegrees(Math.asin(d.y));
        float yaw = (float) Math.toDegrees(Math.atan2(d.z, d.x));

        transform.rotation.x = pitch;
        transform.rotation.y = yaw;
    }

    /**
     * Recomputes the direction vector from transform.rotation
     * (rotation.y = yaw, rotation.x = pitch), matching the same
     * convention used by ControllableCamera.
     */
    private void updateDirectionFromRotation() {
        float yaw = transform.rotation.y;
        float pitch = transform.rotation.x;

        float x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float y = (float) Math.sin(Math.toRadians(pitch));
        float z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        direction.set(x, y, z).normalize();
    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();
        if (_ownsTransform) {
            transform.OnEditorInspect();
        }

        ImGui.spacing();
        ImGui.text("Directional Light");
        ImGui.separator();
        ImGui.spacing();
        ImGui.indent(16.0f);

        // direction is derived from the transform's rotation
        updateDirectionFromRotation();
        ImGui.text(String.format("Direction: (%.2f, %.2f, %.2f)", direction.x, direction.y, direction.z));

        ambientArr[0] = ambient.x;
        ambientArr[1] = ambient.y;
        ambientArr[2] = ambient.z;
        if (ImGui.colorEdit3("Ambient", ambientArr)) {
            ambient.set(ambientArr[0], ambientArr[1], ambientArr[2]);
        }

        diffuseArr[0] = diffuse.x;
        diffuseArr[1] = diffuse.y;
        diffuseArr[2] = diffuse.z;
        if (ImGui.colorEdit3("Diffuse", diffuseArr)) {
            diffuse.set(diffuseArr[0], diffuseArr[1], diffuseArr[2]);
        }

        specularArr[0] = specular.x;
        specularArr[1] = specular.y;
        specularArr[2] = specular.z;
        if (ImGui.colorEdit3("Specular", specularArr)) {
            specular.set(specularArr[0], specularArr[1], specularArr[2]);
        }

        ImGui.unindent(16.0f);
    }

    public Transform getTransform() {
        return transform;
    }

    public static Matrix4f getLightMatrix() {
        return new Matrix4f().ortho(-50f, 50f, -50f, 50f, 0.1f, 100f);
    }

    public static Matrix4f calculateLightSpaceMatrix(Vector3f directionOrPosition, Matrix4f lightMatrix) {
        Vector3f dir = directionOrPosition;
        Matrix4f lightView = new Matrix4f().lookAt(dir, new Vector3f(0), new Vector3f(0, 1, 0));
        return new Matrix4f(lightMatrix).mul(lightView);
    }
}
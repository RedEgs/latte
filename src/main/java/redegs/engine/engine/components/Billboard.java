package redegs.engine.engine.components;

import imgui.ImGui;
import org.joml.Vector3f;
import redegs.engine.graphics.*;

public class Billboard extends Model {
    private Mesh mesh;
    private Texture texture;
    private float size = 1.0f;
    private boolean ylock = false;
    private boolean onTop = true;

    public Billboard(Texture texture) {
        super();
        this.name = "BillboardComponent";
        this.mesh = MeshPrimitives.quad();
        this.texture = texture;

    }

    public Billboard(String path) {
        super();
        this.name = "BillboardComponent";
        this.mesh = MeshPrimitives.quad();
        this.texture = new Texture(path);
    }

    public Billboard(Texture texture, int entity) {
        super(entity);
        this.name = "BillboardComponent";
        this.mesh = MeshPrimitives.quad();
        this.texture = texture;

    }

    public Billboard(String path, int entity) {
        super(entity);
        this.name = "BillboardComponent";
        this.mesh = MeshPrimitives.quad();
        this.texture = new Texture(path);
    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        ImGui.spacing();
        ImGui.text("Billboard");
        ImGui.separator();
        ImGui.spacing();
        ImGui.checkbox("On Top", onTop);
        ImGui.spacing();
        ImGui.indent(16.0f);


        texture.OnEditorInspect();

        ImGui.unindent();
    }

    @Override
    public void Draw(Shader shader) {
        shader.setUniformMat4("model", getModelMatrix());
        texture.use(texture.getId(), shader, "tex");
        mesh.DrawNoMaterials(shader);
    }

    public void setPosition(Vector3f pos) {
        this.transform.position.set(pos);

        this.getModelMatrix().identity();
        this.getModelMatrix().translate(
                this.transform.position.x,
                this.transform.position.y,
                this.transform.position.z
        );
    }

    public Vector3f getPosition() {
        return transform.position;
    }

    public void setOnTop(boolean value) {
        this.onTop = value;
    }

    public boolean getOnTop() {
        return this.onTop;
    }
}

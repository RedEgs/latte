package redegs.engine.engine.components;

import org.joml.Vector3f;
import redegs.engine.graphics.*;

public class Billboard extends Model {
    private Mesh mesh;
    private Texture texture;
    private float size = 1.0f;
    private boolean ylock = false;


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
}

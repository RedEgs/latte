package redegs.engine.graphics;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private List<Mesh> meshes;
    private Matrix4f model_matrix;

    public Model() {
        this.meshes = new ArrayList<>();
        this.model_matrix = new Matrix4f().identity();
    }

    public void Draw(Shader shader) {
        for (int i = 0; i < meshes.size(); i++) {
            Mesh m = meshes.get(i);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer modelBuffer = stack.mallocFloat(16);
                model_matrix.get(modelBuffer);
                shader.setUniformMat4("model", modelBuffer);
            }

            m.Draw(shader);
        }
    }

    public static Model fromMesh(Mesh mesh) {
        Model m = new Model();
        m.meshes.add(mesh);

        return m;
    }

    public Matrix4f getModelMatrix() {
        return this.model_matrix;
    }

}

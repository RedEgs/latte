package redegs.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private final List<Mesh> meshes;
    private final Transform transform = new Transform();

    public Model() {
        this.meshes = new ArrayList<>();
        this.transform.model_matrix = new Matrix4f().identity();
    }

    public Model(String path) {
        this.meshes = new ArrayList<>();
        this.transform.model_matrix = new Matrix4f().identity();

        // Try to load with Assimp first, fall back to OBJ loader
        try {
            Model loadedModel = ModelLoader.loadModel(path);
            this.meshes.addAll(loadedModel.meshes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Draw(Shader shader) {
        for (Mesh m : meshes) {
            shader.setUniformMat4("model", getModelMatrix());
            m.Draw(shader);
        }
    }

    public void centerOrigin() {
        // 1. Accumulate all vertex positions across all meshes
        Vector3f sum = new Vector3f(0, 0, 0);
        int totalVertices = 0;

        for (Mesh mesh : meshes) {
            for (Vertex v : mesh.getVertices()) {
                v.Position.rewind();
                sum.x += v.Position.get();
                sum.y += v.Position.get();
                sum.z += v.Position.get();
                totalVertices++;
            }
        }

        if (totalVertices == 0) return;

        // 2. Calculate centroid
        Vector3f centroid = sum.div(totalVertices);

        // 3. Apply negative centroid as a translation offset
        transform.model_matrix.translate(-centroid.x, -centroid.y, -centroid.z);
    }

    public void addMesh(Mesh mesh) {
        this.meshes.add(mesh);
    }

    public List<Mesh> getMeshes() {
        return new ArrayList<>(meshes);
    }

    public static Model fromMesh(Mesh mesh) {
        Model m = new Model();
        m.meshes.add(mesh);
        return m;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getModelMatrix() {
        return this.transform.model_matrix;
    }

    public void setModelMatrix(Matrix4f matrix) {
        this.transform.model_matrix = matrix;
    }
}
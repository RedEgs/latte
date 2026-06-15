package redegs.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.components.BoundingBox;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.EntitySceneManager;

import java.util.ArrayList;
import java.util.List;

public class Model extends Component {
    protected final List<Mesh> meshes;
    protected Transform transform;

    protected Boolean _renderInDebug = true;

    // Offset applied before the transform, used by centerOrigin()
    protected final Vector3f originOffset = new Vector3f(0, 0, 0);
    protected BoundingBox cachedBoundingBox = null;

    public Model() {
        super(EntitySceneManager.getInstance().createEntity());
        this.name = "ModelComponent";

        this.meshes = new ArrayList<>();
        this.transform = new Transform(getEntity());
        this.transform.model_matrix = new Matrix4f().identity();
        computeBoundingBox();
    }

    public Model(String path) {
        super(EntitySceneManager.getInstance().createEntity());
        this.name = "ModelComponent";

        this.meshes = new ArrayList<>();
        this.transform = new Transform(getEntity());
        this.transform.model_matrix = new Matrix4f().identity();

        // Try to load with Assimp first, fall back to OBJ loader
        try {
            Model loadedModel = ModelLoader.loadModel(path);
            this.meshes.addAll(loadedModel.meshes);
            computeBoundingBox();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Draw(Shader shader) {
        updateModelMatrix();

        for (Mesh m : meshes) {
            shader.setUniformMat4("model", getModelMatrix());
            m.Draw(shader);
        }
    }

    public void DrawNoMaterials(Shader shader) {
        updateModelMatrix();

        for (Mesh m : meshes) {
            shader.setUniformMat4("model", getModelMatrix());
            m.DrawNoMaterials(shader);
        }
    }

    @Override
    public void OnEditorSelect() {
        super.OnEditorSelect();
        if (_renderInDebug) {
            EntitySceneManager.getInstance().getRenderer().selectModel(this);
        }

    }

    @Override
    public void OnEditorDeselect() {
        super.OnEditorDeselect();
        if (_renderInDebug) {
            EntitySceneManager.getInstance().getRenderer().selectModel(null);

        }
    }


    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        if (_renderInDebug) {
            EntitySceneManager esm = EntitySceneManager.getInstance();

            float[] viewMatrix = new float[16];
            float[] projMatrix = new float[16];
            float[] modelMatrix = new float[16];

            esm.GetScene().camera.getView().get(viewMatrix);
            esm.GetScene().camera.getProjection().get(projMatrix);

            updateModelMatrix();
            getModelMatrix().get(modelMatrix);

            transform.OnEditorInspect();
            cachedBoundingBox.OnEditorInspect();
        }
    }

    /**
     * Rebuilds transform.model_matrix from the transform's position,
     * rotation (degrees, XYZ order) and scale, with the origin offset
     * (set via centerOrigin()) applied first.
     */
    private void updateModelMatrix() {
        transform.model_matrix.identity()
                .translate(transform.position)
                .rotateXYZ(
                        (float) Math.toRadians(transform.rotation.x),
                        (float) Math.toRadians(transform.rotation.y),
                        (float) Math.toRadians(transform.rotation.z)
                )
                .scale(transform.scale)
                .translate(originOffset);
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

        // 3. Store negative centroid as a permanent offset, applied
        //    every time updateModelMatrix() rebuilds the matrix.
        originOffset.set(-centroid.x, -centroid.y, -centroid.z);
    }

    public BoundingBox computeBoundingBox() {
        if (cachedBoundingBox != null) return cachedBoundingBox;

        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (Mesh mesh : meshes) {
            for (Vertex v : mesh.getVertices()) {
                v.Position.rewind();
                float x = v.Position.get();
                float y = v.Position.get();
                float z = v.Position.get();

                min.min(new Vector3f(x, y, z));
                max.max(new Vector3f(x, y, z));
            }
        }

        cachedBoundingBox = new BoundingBox(min, max, entity);
        return cachedBoundingBox;
    }

    // call this if you ever modify the mesh vertices at runtime
    public void invalidateBoundingBox() {
        cachedBoundingBox = null;
    }



    public void addMesh(Mesh mesh) {
        this.meshes.add(mesh);
        computeBoundingBox();
    }

    public List<Mesh> getMeshes() {
        return new ArrayList<>(meshes);
    }

    public static Model fromMesh(Mesh mesh) {
        Model m = new Model();
        m.meshes.add(mesh);
        m.computeBoundingBox();

        return m;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getModelMatrix() {
        updateModelMatrix();
        return this.transform.model_matrix;
    }

    public void setModelMatrix(Matrix4f matrix) {
        this.transform.model_matrix = matrix;
    }

    public void disableRenderInDebug() {
        this._renderInDebug = false;
    }

    public void enableRenderInDebug() {
        this._renderInDebug = true;
    }
}
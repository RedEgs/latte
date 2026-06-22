package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.components.BoundingBox;
import redegs.engine.engine.gson.Save;
import redegs.engine.engine.system.FileDialogs;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;

import java.util.ArrayList;
import java.util.List;

@ComponentMeta(name = "Model", category = "Geometry", description = "A Collection of meshes with a transform.")
public class Model extends Component {
    protected List<Mesh> meshes;
    protected Transform transform;

    protected Boolean _renderInDebug = true;
    protected Boolean _ownsTransform = false;

    // Offset applied before the transform, used by centerOrigin()
    protected final Vector3f originOffset = new Vector3f(0, 0, 0);
    protected transient BoundingBox cachedBoundingBox = null;
    protected String location;

    public Model() {
        super(EntitySceneManager.getInstance().createEntity());
        //EntitySceneManager.getInstance().addComponent(entity, this);
        init();
    }


    public Model(int entity) {
        super(entity);
        init();
    }

    public Model(int entity, String path) {
        super(entity);
        init(path);
    }

    static {
        ComponentRegistry.register(
                Model.class,
                entity -> new Model(entity)
        );

    }

    public void init() {
        this.name = "ModelComponent";
        if (location != null) {
            this.init(location);
        }


        this.meshes = new ArrayList<>();
        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            _ownsTransform = false;

        } else {
            this.transform = new Transform(entity);
            EntitySceneManager.getInstance().addComponent(entity, this.transform);
            _ownsTransform = true;
        }
        this.transform.model_matrix = new Matrix4f().identity();
        updateModelMatrix();
        computeBoundingBox();
    }

    public void init(String path) {
        this.name = "ModelComponent";
        this.location = path;

        this.meshes = new ArrayList<>();
        if (entityHas(Transform.class)) {
            System.out.println("Found trnasfomr");
            this.transform = entityGet(Transform.class);
            _ownsTransform = false;
        } else {
            this.transform = new Transform(entity);
            EntitySceneManager.getInstance().addComponent(entity, this.transform);
            _ownsTransform = true;
        }
        this.transform.model_matrix = new Matrix4f().identity();


        // Try to load with Assimp first, fall back to OBJ loader
        try {
            this.meshes.addAll(ModelLoader.load(path, entity));
            computeBoundingBox();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.addProperty("location", location);

        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);

        location = data.get("location").getAsString();
        init(location);
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
    public void OnUpdate() {
        super.OnUpdate();
        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            this._ownsTransform = false;
        }
    }

    @Override
    public void OnDelete() {
        super.OnDelete();
        if (_renderInDebug) {
            EntitySceneManager.getInstance().getRenderer().selectModel(null);
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

            ImGui.text("Location (Path): " + location); ImGui.sameLine();
            if (ImGui.button("Load .gltf ...")) {
                String path = FileDialogs.openFile("Select .gltf...", FileDialogs.homeDirectory(), new String[]{"*.gltf"}, ".gltf files");
                this.location = path;
                this.meshes.addAll(ModelLoader.load(path, entity));
                computeBoundingBox();
            }

            cachedBoundingBox.OnEditorInspect();
        }
    }

    /**
     * Rebuilds transform.model_matrix from the transform's position,
     * rotation (degrees, XYZ order) and scale, with the origin offset
     * (set via centerOrigin()) applied first.
     */
    public void updateModelMatrix() {
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

    public BoundingBox getBoundingBox() {
        return cachedBoundingBox;
    }
    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getModelMatrix() {
        updateModelMatrix();
        return this.transform.model_matrix;
    }

    public String getPath() {
        return this.location;
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
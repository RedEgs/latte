package redegs.engine.graphics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import redegs.engine.engine.components.BoundingBox;
import redegs.engine.engine.gson.Save;
import redegs.engine.engine.system.FileDialogs;
import redegs.engine.engine.system.asset.ModelAsset;
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

        // Only compute bounding box if we actually have meshes (e.g. loaded via location)
        if (location != null) {
            init(location);
        } else {
            invalidateBoundingBox();
            computeBoundingBox();
        }
    }

    public void init(String path) {
        this.name = "ModelComponent";
        this.location = path;
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

        try {
            loadFromFile(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.addProperty("location", location);

        JsonArray meshMaterials = new JsonArray();
        for (Mesh mesh : meshes) {
            JsonArray materialsJson = new JsonArray();
            for (Material material : mesh.materials) {
                materialsJson.add(material != null ? material.Save() : null);
            }
            meshMaterials.add(materialsJson);
        }
        o.add("meshMaterials", meshMaterials);

        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);
        if (data.has("location") && !data.get("location").isJsonNull()) {
            location = data.get("location").getAsString();
            init(location);
        }
        if (data.has("meshMaterials") && data.get("meshMaterials").isJsonArray()) {
            loadMaterialOverrides(data.getAsJsonArray("meshMaterials"));
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
    public void OnUpdate() {
        super.OnUpdate();
        // Sync transform reference if another component owns it
        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            this._ownsTransform = false;
        }
        // Note: bounding box is NOT recomputed here — call invalidateBoundingBox()
        // + computeBoundingBox() explicitly if mesh geometry changes at runtime.
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

            ImGui.text("Location (Path): " + getDisplayLocation());
            ImGui.sameLine();
            if (ImGui.button("Import .gltf Asset ...")) {
                String path = FileDialogs.openFile("Select .gltf...", FileDialogs.homeDirectory(), new String[]{"*.gltf", "*.glb"}, "Model files");
                if (path != null && !path.isBlank()) {
                    loadAsset(ModelAsset.importFile(path));
                }
            }

            if (location != null && !location.isBlank()) {
                ImGui.sameLine();
                if (ImGui.button("Reload")) {
                    loadFromFile(location);
                }
            }

            if (ImGui.collapsingHeader("Model Assets")) {
                ImGui.indent(16.0f);
                ArrayList<ModelAsset> modelAssets = new ArrayList<>(redegs.engine.engine.system.asset.AssetManager.getAll(ModelAsset.class));
                if (modelAssets.isEmpty()) {
                    ImGui.text("No model assets imported.");
                }

                for (int i = 0; i < modelAssets.size(); i++) {
                    ModelAsset asset = modelAssets.get(i);
                    ImGui.pushID(i);

                    String label = asset.getName();
                    if (asset.getPath().equals(location)) {
                        label += " (current)";
                    }

                    if (ImGui.selectable(label)) {
                        loadAsset(asset);
                    }
                    ImGui.setItemTooltip(asset.getPath());

                    ImGui.popID();
                }
                ImGui.unindent(16.0f);
            }

            ImGui.indent(16.0f);

            if (cachedBoundingBox != null) {
                if (ImGui.collapsingHeader("Bounding Box")) {
                    cachedBoundingBox.OnEditorInspect();
                    ImGui.spacing();
                }
            }

            if (ImGui.collapsingHeader("Materials")) {
                ImGui.indent(16.0f);

                ImGui.pushStyleVar(ImGuiStyleVar.ImageBorderSize, 1.0f);

                for (int meshIndex = 0; meshIndex < meshes.size(); meshIndex++) {
                    Mesh mesh = meshes.get(meshIndex);
                    for (int materialIndex = 0; materialIndex < mesh.materials.size(); materialIndex++) {
                        ImGui.pushID(meshIndex + "-" + materialIndex);

                        Material material = mesh.materials.get(materialIndex);
                        if (ImGui.collapsingHeader("Mesh " + meshIndex + " Material " + materialIndex)) {
                            Material selected = material.drawEditorImage(true);
                            if (selected != null) {
                                mesh.materials.set(materialIndex, selected);
                                material = selected;
                            }

                            material.drawEditorProperties(true);
                        }

                        ImGui.popID();
                    }
                }

                ImGui.popStyleVar();

                ImGui.unindent(16.0f);
            }

            ImGui.unindent(16.0f);

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

        Vector3f centroid = sum.div(totalVertices);
        originOffset.set(-centroid.x, -centroid.y, -centroid.z);
    }

    /**
     * Computes the bounding box in local (mesh) space.
     * Call invalidateBoundingBox() first if meshes have changed.
     */
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

    /**
     * Returns the bounding box transformed into world space by the current model matrix.
     * Use this for rendering the debug box or any world-space intersection tests.
     * The local cachedBoundingBox is used as the source — call computeBoundingBox() first.
     */
    public BoundingBox getWorldBoundingBox() {
        if (cachedBoundingBox == null) computeBoundingBox();

        Matrix4f model = getModelMatrix();

        Vector3f localMin = cachedBoundingBox.min;
        Vector3f localMax = cachedBoundingBox.max;

        // Transform all 8 corners of the local AABB into world space,
        // then find the new axis-aligned min/max that contains them all.
        Vector3f[] corners = {
                new Vector3f(localMin.x, localMin.y, localMin.z),
                new Vector3f(localMax.x, localMin.y, localMin.z),
                new Vector3f(localMin.x, localMax.y, localMin.z),
                new Vector3f(localMax.x, localMax.y, localMin.z),
                new Vector3f(localMin.x, localMin.y, localMax.z),
                new Vector3f(localMax.x, localMin.y, localMax.z),
                new Vector3f(localMin.x, localMax.y, localMax.z),
                new Vector3f(localMax.x, localMax.y, localMax.z),
        };

        Vector3f worldMin = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f worldMax = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (Vector3f corner : corners) {
            model.transformPosition(corner);
            worldMin.min(corner);
            worldMax.max(corner);
        }

        return new BoundingBox(worldMin, worldMax, entity);
    }

    public void invalidateBoundingBox() {
        cachedBoundingBox = null;
    }

    public void loadAsset(ModelAsset asset) {
        if (asset != null) {
            loadFromFile(asset.getPath());
        }
    }

    public void loadFromFile(String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        List<Mesh> loadedMeshes = ModelLoader.load(path, entity);
        this.location = path;
        this.meshes.clear();
        this.meshes.addAll(loadedMeshes);
        invalidateBoundingBox();
        computeBoundingBox();
    }

    private String getDisplayLocation() {
        return location == null || location.isBlank() ? "(none)" : location;
    }

    private void loadMaterialOverrides(JsonArray meshMaterials) {
        for (int meshIndex = 0; meshIndex < meshMaterials.size() && meshIndex < meshes.size(); meshIndex++) {
            JsonElement meshElement = meshMaterials.get(meshIndex);
            if (!meshElement.isJsonArray()) {
                continue;
            }

            Mesh mesh = meshes.get(meshIndex);
            JsonArray materialsJson = meshElement.getAsJsonArray();
            for (int materialIndex = 0; materialIndex < materialsJson.size() && materialIndex < mesh.materials.size(); materialIndex++) {
                JsonElement materialElement = materialsJson.get(materialIndex);
                if (materialElement == null || materialElement.isJsonNull() || !materialElement.isJsonObject()) {
                    continue;
                }

                String fallbackName = "entity" + entity + "_mesh" + meshIndex + "_material" + materialIndex;
                mesh.materials.set(materialIndex, Material.fromJson(materialElement.getAsJsonObject(), entity, fallbackName));
            }
        }
    }

    public void addMesh(Mesh mesh) {
        this.meshes.add(mesh);
        invalidateBoundingBox();
        computeBoundingBox();
    }

    public List<Mesh> getMeshes() {
        return new ArrayList<>(meshes);
    }

    public static Model fromMesh(Mesh mesh) {
        Model m = new Model();
        m.meshes.add(mesh);
        m.invalidateBoundingBox();
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

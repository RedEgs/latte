package redegs.engine.engine.components;
import com.google.gson.JsonObject;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import redegs.engine.engine.gson.Save;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.graphics.Mesh;
import redegs.engine.graphics.Vertex;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class BoundingBox extends Component {
    public Vector3f min;
    public Vector3f max;
    public Mesh mesh;

    public BoundingBox(Vector3f min, Vector3f max, int entity) {
        super(entity);
        this.name = "BoundingBoxComponent";
        this.min = min;
        this.max = max;
    }

    public BoundingBox(Vector3f min, Vector3f max) {
        super(EntitySceneManager.getInstance().createEntity());
        this.name = "BoundingBoxComponent";
        this.min = min;
        this.max = max;
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.add("min", Save.Vec3ToJson(min));
        o.add("max", Save.Vec3ToJson(max));
        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);
        min = Save.JsonToVec3(data.getAsJsonObject("min"));
        max = Save.JsonToVec3(data.getAsJsonObject("max"));

    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        ImGui.spacing();
        ImGui.text("Bounding Box");
        ImGui.separator();
        ImGui.spacing();
        ImGui.indent(16.0f);

        Vector3f size = getSize();
        Vector3f center = getCenter();

        ImGui.text(String.format("Min:    (%.2f, %.2f, %.2f)", min.x, min.y, min.z));
        ImGui.text(String.format("Max:    (%.2f, %.2f, %.2f)", max.x, max.y, max.z));
        ImGui.text(String.format("Center: (%.2f, %.2f, %.2f)", center.x, center.y, center.z));
        ImGui.text(String.format("Size:   (%.2f, %.2f, %.2f)", size.x, size.y, size.z));
        ImGui.text(String.format("Radius: %.2f", getRadius()));

        ImGui.unindent(16.0f);

    }

    public Vector3f getCenter() {
        return new Vector3f(min).add(max).mul(0.5f);
    }

    public Vector3f getSize() {
        return new Vector3f(max).sub(min);
    }

    public float getRadius() {
        return getSize().length() * 0.5f;
    }

    public static BoundingBox computeBoundingBox(Mesh mesh) {
        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (Vertex v : mesh.getVertices()) {
            v.Position.rewind();
            float x = v.Position.get();
            float y = v.Position.get();
            float z = v.Position.get();

            min.x = Math.min(min.x, x);
            min.y = Math.min(min.y, y);
            min.z = Math.min(min.z, z);

            max.x = Math.max(max.x, x);
            max.y = Math.max(max.y, y);
            max.z = Math.max(max.z, z);
        }

        return new BoundingBox(min, max);
    }

    public static BoundingBox merge(BoundingBox a, BoundingBox b) {
        Vector3f min = new Vector3f(
                Math.min(a.min.x, b.min.x),
                Math.min(a.min.y, b.min.y),
                Math.min(a.min.z, b.min.z)
        );
        Vector3f max = new Vector3f(
                Math.max(a.max.x, b.max.x),
                Math.max(a.max.y, b.max.y),
                Math.max(a.max.z, b.max.z)
        );
        return new BoundingBox(min, max);
    }

    public BoundingBox getWorldBoundingBox(BoundingBox local, Matrix4f modelMatrix) {
        Vector3f[] corners = new Vector3f[8];
        corners[0] = new Vector3f(local.min.x, local.min.y, local.min.z);
        corners[1] = new Vector3f(local.max.x, local.min.y, local.min.z);
        corners[2] = new Vector3f(local.min.x, local.max.y, local.min.z);
        corners[3] = new Vector3f(local.max.x, local.max.y, local.min.z);
        corners[4] = new Vector3f(local.min.x, local.min.y, local.max.z);
        corners[5] = new Vector3f(local.max.x, local.min.y, local.max.z);
        corners[6] = new Vector3f(local.min.x, local.max.y, local.max.z);
        corners[7] = new Vector3f(local.max.x, local.max.y, local.max.z);

        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (Vector3f corner : corners) {
            modelMatrix.transformPosition(corner);
            min.min(corner);
            max.max(corner);
        }

        return new BoundingBox(min, max);
    }

    public Mesh toWireframeMesh() {
        Vector3f mn = min;
        Vector3f mx = max;

        Vector3f zero3 = new Vector3f(0, 0, 0);
        Vector2f zero2 = new Vector2f(0, 0);

        List<Vertex> vertices = new ArrayList<>();
        vertices.add(Vertex.createVertex(new Vector3f(mn.x, mn.y, mn.z), zero3, zero2)); // 0 left  bottom back
        vertices.add(Vertex.createVertex(new Vector3f(mx.x, mn.y, mn.z), zero3, zero2)); // 1 right bottom back
        vertices.add(Vertex.createVertex(new Vector3f(mn.x, mx.y, mn.z), zero3, zero2)); // 2 left  top    back
        vertices.add(Vertex.createVertex(new Vector3f(mx.x, mx.y, mn.z), zero3, zero2)); // 3 right top    back
        vertices.add(Vertex.createVertex(new Vector3f(mn.x, mn.y, mx.z), zero3, zero2)); // 4 left  bottom front
        vertices.add(Vertex.createVertex(new Vector3f(mx.x, mn.y, mx.z), zero3, zero2)); // 5 right bottom front
        vertices.add(Vertex.createVertex(new Vector3f(mn.x, mx.y, mx.z), zero3, zero2)); // 6 left  top    front
        vertices.add(Vertex.createVertex(new Vector3f(mx.x, mx.y, mx.z), zero3, zero2)); // 7 right top    front

        int[] edgeIndices = {
                // back face
                0, 1,  1, 3,  3, 2,  2, 0,
                // front face
                4, 5,  5, 7,  7, 6,  6, 4,
                // connecting edges
                0, 4,  1, 5,  2, 6,  3, 7
        };

        IntBuffer indices = BufferUtils.createIntBuffer(edgeIndices.length);
        for (int i : edgeIndices) indices.put(i);
        indices.flip();

        this.mesh = new Mesh(vertices, indices, new ArrayList<>());
        return mesh;
    }
}
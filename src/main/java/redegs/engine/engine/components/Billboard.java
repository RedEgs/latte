package redegs.engine.engine.components;

import com.google.gson.JsonObject;
import imgui.ImGui;
import org.joml.Vector3f;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.FileDialogs;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;
import redegs.engine.graphics.*;

@ComponentMeta(name = "Billboard", category = "3D", description = "A 3D Camera-Fixed Quad with a Texture.")
public class Billboard extends Component  {
    protected Mesh mesh;
    protected Texture texture;
    protected Transform transform;

    protected boolean _ownsTransform;

    protected float size = 1.0f;
    protected boolean ylock = false;
    protected boolean ontop = true;

    public Billboard(Texture texture) {
        super(EntitySceneManager.getInstance().createEntity());
        init();
        this.texture = texture;
    }

    public Billboard(String path) {
        super(EntitySceneManager.getInstance().createEntity());
        init();
        this.texture = new Texture(path);
    }

    public Billboard(Texture texture, int entity) {
        super(entity);
        init();
        this.texture = texture;

    }

    public Billboard(String path, int entity) {
        super(entity);
        init();
        this.texture = new Texture(path);

    }

    public Billboard(int entity) {
        super(entity);
        init();
    }

    public void init() {
        this.name = "BillboardComponent";
        this.mesh = MeshPrimitives.quad();
        this.texture = null;

        if (entityHas(Transform.class)) {
            this.transform = entityGet(Transform.class);
            _ownsTransform = false;

        } else {
            this.transform = new Transform(entity);
            EntitySceneManager.getInstance().addComponent(entity, this.transform);
            _ownsTransform = true;
        }
        setPosition(transform.position);
    }

    static {
        ComponentRegistry.register(
                Billboard.class,
                entity -> new Billboard(entity)
        );
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.add("texture", texture.Save());
        o.addProperty("size", size);
        o.addProperty("ylock", ylock);
        o.addProperty("ontop", ontop);

        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);
        texture = Texture.fromJson(data.getAsJsonObject("texture"), entity);
        size = data.get("size").getAsFloat();
        ylock = data.get("ylock").getAsBoolean();
        ontop = data.get("ontop").getAsBoolean();
    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        ImGui.spacing();
        ImGui.text("Billboard");
        ImGui.separator();
        ImGui.spacing();
        ImGui.checkbox("On Top", ontop);
        ImGui.checkbox("Y-Lock", ylock);

        String l = null;
        if (texture !=  null) {
            l = texture.getLocation();
        }
        ImGui.text("Location: " + l);
        ImGui.sameLine();
        if (ImGui.button("Load from folder ...")) {
            String path = FileDialogs.openFile("Select image file...", FileDialogs.homeDirectory(), new String[]{"*.png"}, "Image files");
            this.texture = new Texture(path, entity);
        }
        ImGui.spacing();
        ImGui.indent(16.0f);

        if (texture != null) {
            texture.OnEditorInspect();
        }


        ImGui.unindent();
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
    public void Draw(Shader shader) {
        if (texture == null) return;

        shader.setUniformMat4("model", transform.model_matrix);
        texture.use(texture.getId(), shader, "tex");
        mesh.DrawNoMaterials(shader);
    }

    public void setPosition(Vector3f pos) {
        this.transform.position.set(pos);

        transform.model_matrix.identity();
        transform.model_matrix.translate(
                this.transform.position.x,
                this.transform.position.y,
                this.transform.position.z
        );
    }

    public Vector3f getPosition() {
        return transform.position;
    }

    public void setOntop(boolean value) {
        this.ontop = value;
    }

    public boolean getOntop() {
        return this.ontop;
    }
}

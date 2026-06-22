package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import org.w3c.dom.Text;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.FileDialogs;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;
import redegs.engine.graphics.lights.PointLightSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;

@ComponentMeta(name = "Cubemap", category = "3D", description = "A Skybox compatible cubemap.")
public class Cubemap extends Component {
    public enum Face {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM,
        BACK,
        FRONT
    }

    private transient int id;
    protected List<Texture> textures;
    protected String location;

    public Cubemap(int entity) {
        super(entity);
        name = "CubemapComponent";

        id = glGenTextures();
        textures = new ArrayList<>();
    }

    public Cubemap() {
        super(EntitySceneManager.getInstance().createEntity());
        name = "CubemapComponent";

        id = glGenTextures();
        textures = new ArrayList<>();
    }



    static {
        ComponentRegistry.register(
                Cubemap.class,
                entity -> new Cubemap(entity)
        );
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        for (int i = 0; i < textures.size(); i++) {
            o.add("texture" + i, textures.get(i).Save());
        }
//        if (this.location != null) {
//            o.addProperty("location", location);
//        }
        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);

        textures.clear();
//        setTexture(new Texture(Face.RIGHT, entity), Face.RIGHT);
//        setTexture(new Texture(Face.LEFT, entity), Face.LEFT);
//        setTexture(new Texture(Face.TOP, entity), Face.TOP);
//        setTexture(new Texture(Face.BOTTOM, entity), Face.BOTTOM);
//        setTexture(new Texture(Face.BACK, entity), Face.BACK);
//        setTexture(new Texture(Face.FRONT, entity), Face.FRONT);

        for (int i = 0; i < 6; i++) {
            Face f = Face.values()[i];
            Texture t = new Texture(f, entity);
            t.Load(data.getAsJsonObject("texture" + i));
            setTexture(t, f);
        }
//        if (data.get("location") != null) {
//            fromFile(data.get("location").getAsString());
//        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    public void setTexture(Texture texture, Face face) {
        while (textures.size() <= face.ordinal())
            textures.add(null);

        textures.set(face.ordinal(), texture);
    }


    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();
        ImGui.text("Location: " + location);

        for (Texture texture : textures) {
            texture.OnEditorInspect();
        }
        if (ImGui.button("Load from folder ...")) {
            String path = FileDialogs.selectFolder("Select cubemap folder...", FileDialogs.homeDirectory());
            fromFile(path);
        }
    }

    public void fromFile(String path) {
        this.location = path;
        List<File> files = List.of(Objects.requireNonNull(new File(path).listFiles()));
        HashMap<String, File> filenames = new HashMap<String, File>();

        for (File file : files) {
            if (file.isFile()) {
                System.out.println(file.getName().toLowerCase());
                filenames.put(file.getName().toLowerCase(), file);
            }
        }
        setTexture(new Texture(filenames.get("right.jpg").toPath().toString(), Face.RIGHT, entity), Face.RIGHT);
        setTexture(new Texture(filenames.get("left.jpg").toPath().toString(), Face.LEFT, entity), Face.LEFT);
        setTexture(new Texture(filenames.get("top.jpg").toPath().toString(), Face.TOP, entity), Face.TOP);
        setTexture(new Texture(filenames.get("bottom.jpg").toPath().toString(), Face.BOTTOM, entity), Face.BOTTOM);
        setTexture(new Texture(filenames.get("front.jpg").toPath().toString(), Face.BACK, entity), Face.BACK);
        setTexture(new Texture(filenames.get("back.jpg").toPath().toString(), Face.FRONT, entity), Face.FRONT);
    }

    public static Cubemap fromFile(String path, int entity) {
        Cubemap cubemap = new Cubemap(entity);
        cubemap.fromFile(path);
        return cubemap;
    }


    public int getId() {
        return id;
    }
}

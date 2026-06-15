package redegs.engine.graphics;

import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;

public class Cubemap extends Component {
    public enum Face {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM,
        BACK,
        FRONT
    }

    private int id;
    protected List<Texture> textures;

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

    public void bind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    public void setTexture(Texture texture, Face face) {
        if (texture.getTextureType() != Texture.TextureType.CUBEMAP)
            throw new RuntimeException("Texture type must be CUBEMAP.");

        textures.add(face.ordinal(), texture);
    }

    public static Cubemap fromFile(String path) {
        List<File> files = List.of(Objects.requireNonNull(new File(path).listFiles()));
        HashMap<String, File> filenames = new HashMap<String, File>();

        for (File file : files) {
            if (file.isFile()) {
                System.out.println(file.getName().toLowerCase());
                filenames.put(file.getName().toLowerCase(), file);
            }
        }
        Cubemap cubemap = new Cubemap();

        cubemap.setTexture(new Texture(filenames.get("right.jpg").toPath().toString(), Face.RIGHT), Face.RIGHT);
        cubemap.setTexture(new Texture(filenames.get("left.jpg").toPath().toString(), Face.LEFT), Face.LEFT);
        cubemap.setTexture(new Texture(filenames.get("top.jpg").toPath().toString(), Face.TOP), Face.TOP);
        cubemap.setTexture(new Texture(filenames.get("bottom.jpg").toPath().toString(), Face.BOTTOM), Face.BOTTOM);
        cubemap.setTexture(new Texture(filenames.get("front.jpg").toPath().toString(), Face.BACK), Face.BACK);
        cubemap.setTexture(new Texture(filenames.get("back.jpg").toPath().toString(), Face.FRONT), Face.FRONT);

        return cubemap;
    }

    public int getId() {
        return id;
    }
}

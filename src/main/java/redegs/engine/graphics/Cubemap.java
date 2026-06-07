package redegs.engine.graphics;

import java.util.List;

import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;

public class Cubemap {
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

    public Cubemap() {
        id = glGenTextures();
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    public void setTexture(Texture texture, Face face) {
        textures.add(texture);
    }
}

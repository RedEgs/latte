package redegs.engine.graphics;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL21C.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

public class Texture {
    public enum TextureType {
        COLOR,
        DEPTH,
        POSITION,
        NORMALS,
    }

    private int id;
    private int width, height;

    private Image image = null;
    private TextureType type = null;

    private boolean setup = false;

    public Texture(String path_to_texture) {
        id = glGenTextures();
        type = TextureType.COLOR;

        fromFile(path_to_texture);

        setup = true;
    }
    public Texture(int width, int height, TextureType type) {
        this.type = type;
        createAttachment(width, height);
        setup = true;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
    public void unbind() { glBindTexture(GL_TEXTURE_2D, 0);}

    public void use(int texureUnit) {
        glActiveTexture(GL_TEXTURE0+texureUnit);
        glBindTexture(GL_TEXTURE_2D, id);
    }
    public void use(int texureUnit, Shader shader, String name) {
        glActiveTexture(GL_TEXTURE0+texureUnit);
        glBindTexture(GL_TEXTURE_2D, id);
        shader.setUniform1i(name, texureUnit);
    }

    public void resize(int width, int height) {
        ;
    }

    private void fromFile(String path_to_texture) {
        if (this.setup == true) {
            System.err.println("Cannot load texture from file on already existing texture.");
        };

        image = new Image(path_to_texture);
        ByteBuffer bytes = image.getByteBuffer();
        bytes.flip();

        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
        glGenerateMipmap(GL_TEXTURE_2D);

        this.width = image.bufferedImage.getWidth();
        this.height = image.bufferedImage.getHeight();
    }


    private void createAttachment(int width, int height) {
        if (this.setup == true) {
            System.err.println("Cannot create attachment on existing texture");
            return;
        }

        this.id = glGenTextures();
        this.width = width;
        this.height = height;

        glBindTexture(GL_TEXTURE_2D, id);

        if (this.type == TextureType.DEPTH) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24,
                    width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
            float[] border = {1.0f, 1.0f, 1.0f, 1.0f};
            glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, border);
        }
        else if (this.type == TextureType.POSITION || this.type == TextureType.NORMALS) {
            // FIXED: Use GL_RGBA16F as internal format, GL_RGBA as format, GL_FLOAT as type
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA16F,      // Internal format (how OpenGL stores it)
                    width,
                    height,
                    0,
                    GL_RGBA,         // Format (how you provide the data - even if null)
                    GL_FLOAT,        // Type (data type of each component)
                    (ByteBuffer) null
            );
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }
        else { // COLOR type
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,        // Internal format
                    width,
                    height,
                    0,
                    GL_RGBA,         // Format
                    GL_UNSIGNED_BYTE, // Type
                    (ByteBuffer) null
            );
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }
    }

    public int getId() {
        return this.id;
    }

}

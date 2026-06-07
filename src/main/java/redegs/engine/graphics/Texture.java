package redegs.engine.graphics;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL21C.GL_SRGB;
import static org.lwjgl.opengl.GL21C.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

public class Texture {
    public enum AttachmentType {
        COLOR,
        DEPTH,
        POSITION,
        NORMALS,
    }

    public enum TextureType {
        IMAGE,
        ATTACHMENT,
        CUBEMAP
    }

    private int id;
    private int width, height;

    private Image image = null;
    private AttachmentType attachment_type = null;
    private TextureType texture_type = null;

    private boolean setup = false;

    public Texture(String path_to_texture, Cubemap.Face face) {
        id = glGenTextures();
        texture_type = TextureType.CUBEMAP;
        attachment_type = null;

        fromFile(path_to_texture, TextureType.CUBEMAP, face);
        setup = true;
    }
    public Texture(String path_to_texture) {
        id = glGenTextures();
        texture_type = TextureType.IMAGE;
        attachment_type = null;

        fromFile(path_to_texture);

        setup = true;
    }

    public Texture(int width, int height, AttachmentType attachment_type) {
        this.attachment_type = attachment_type;
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
        fromFile(path_to_texture, TextureType.IMAGE, null);
    }

    private void fromFile(String path_to_texture, TextureType type, Cubemap.Face face) {
        if (this.setup == true) {
            System.err.println("Cannot load texture from file on already existing texture.");
        };

        image = new Image(path_to_texture);
        ByteBuffer bytes = image.getByteBuffer();
        bytes.flip();

        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        if (type == TextureType.IMAGE) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
            glGenerateMipmap(GL_TEXTURE_2D);
        } else if (type == TextureType.CUBEMAP) {
            if (face == null) throw new RuntimeException("Must give face index for this type.");
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face.ordinal(),
                    0, GL_SRGB, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        } else {
            throw new RuntimeException("Cannot load texture fromFile of this type.");
        }

        this.width = image.bufferedImage.getWidth();
        this.height = image.bufferedImage.getHeight();
        unbind();
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

        if (this.attachment_type == AttachmentType.DEPTH) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24,
                    width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
            float[] border = {1.0f, 1.0f, 1.0f, 1.0f};
            glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, border);
        } else if (this.attachment_type == AttachmentType.POSITION || this.attachment_type == AttachmentType.NORMALS) {
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

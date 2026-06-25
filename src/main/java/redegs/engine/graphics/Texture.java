package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSliderFlags;
import org.w3c.dom.Text;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;

import javax.imageio.ImageIO;
import javax.swing.text.html.parser.Entity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL21C.GL_SRGB;
import static org.lwjgl.opengl.GL21C.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

public class Texture extends Component {
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

    private transient int id;
    private int width, height;

    private transient Image image = null;
    private AttachmentType attachment_type = null;
    private TextureType texture_type = null;
    private Cubemap.Face face;
    protected String location;

    private boolean setup = false;

    private static Texture defaultTexure;

    public Texture(String path_to_texture, Cubemap.Face face, int entity) {
        super(entity);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.CUBEMAP;
        attachment_type = null;
        this.face = face;

        fromFile(path_to_texture, TextureType.CUBEMAP, face);
        setup = true;
    }
    public Texture(String path_to_texture, int entity) {
        super(entity);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.IMAGE;
        attachment_type = null;

        fromFile(path_to_texture);

        setup = true;
    }
    private Texture(int entity) {
        super(entity);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.IMAGE;
        attachment_type = null;

        setup = false;


    }
    public Texture(int width, int height, AttachmentType attachment_type, int entity) {
        super(entity);
        name = "TextureComponent";

        this.attachment_type = attachment_type;
        createAttachment(width, height);
        setup = true;
    }
    public Texture(String path_to_texture, Cubemap.Face face) {
        super(EntitySceneManager.getInstance().createEntity());
        name = "TextureComponent";
        EntitySceneManager.getInstance().addComponent(entity, this);

        id = glGenTextures();
        texture_type = TextureType.CUBEMAP;
        attachment_type = null;
        this.face = face;

        fromFile(path_to_texture, TextureType.CUBEMAP, face);
        setup = true;
    }
    public Texture(String path_to_texture) {
        super(EntitySceneManager.getInstance().createEntity());
        name = "TextureComponent";
        EntitySceneManager.getInstance().addComponent(entity, this);

        id = glGenTextures();
        texture_type = TextureType.IMAGE;
        attachment_type = null;

        fromFile(path_to_texture);

        setup = true;
    }
    public Texture(int width, int height, AttachmentType attachment_type) {
        super(EntitySceneManager.getInstance().createEntity());
        name = "TextureComponent";
        EntitySceneManager.getInstance().addComponent(entity, this);

        this.attachment_type = attachment_type;
        createAttachment(width, height);
        setup = true;
    }
    public Texture(Cubemap.Face face, int entity) {
        super(entity);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.CUBEMAP;
        attachment_type = null;
        this.face = face;

        setup = false;
    }

    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        if (location != null) {
            o.addProperty("location", location);
        }

        if (attachment_type != null) {
            o.addProperty("attachment_type", attachment_type.ordinal());
        }
        if (texture_type != null) {
            o.addProperty("texture_type", texture_type.ordinal());
        }
        if (face != null) {
            o.addProperty("face", face.ordinal());
        }

        try {
            if (location == null)
                o.addProperty("data", Base64.getEncoder().encodeToString(image.toBytes()));
        } catch (Exception e) {
            System.out.println("failed to write ot img ");
        }

        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);
        String l = null;
        if ( data.get("location") != null) {
            l = data.get("location").getAsString();

        }

        if (data.get("attachment_type") != null) {
            attachment_type = AttachmentType.values()[data.get("attachment_type").getAsInt()];
        }
        if (data.get("texture_type") != null) {
            texture_type = TextureType.values()[data.get("texture_type").getAsInt()];
        }
        if (data.get("face") != null) {
            face = Cubemap.Face.values()[data.get("face").getAsInt()];
        }


        if (l != null) {
            location = l;
            fromFile(l);
        } else {
            String raw = data.get("data").getAsString();
            if (raw != null) {
                byte[] bytes = Base64.getDecoder().decode(raw);
                fromBytes(bytes, texture_type, face);
            }
        }
    }

    public static Texture fromJson(JsonObject data, int entity) {
        Texture t = new Texture(entity);
        t.Load(data);
        return  t;
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
        location = path_to_texture;
        if (this.setup) {
            System.err.println("Cannot load texture from file on already existing texture.");
        };

        image = new Image(path_to_texture);
        ByteBuffer bytes = image.getByteBuffer();

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

    private void fromBytes(byte[] bytes, TextureType type, Cubemap.Face face) {
        if (this.setup) {
            System.err.println("Cannot load texture from file on already existing texture.");
        };

        image = new Image(bytes);
        ByteBuffer bb = image.getByteBuffer();

        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        if (type == TextureType.IMAGE) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
            glGenerateMipmap(GL_TEXTURE_2D);
        } else if (type == TextureType.CUBEMAP) {
            if (face == null) throw new RuntimeException("Must give face index for this type.");
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face.ordinal(),
                    0, GL_SRGB, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
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
        if (this.setup) {
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


    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        ImGui.spacing();
        ImGui.text("Texture");
        ImGui.separator();
        ImGui.spacing();
        ImGui.indent(16.0f);

        ImGui.separatorText("Texture Format");
        if (getTextureType() != null) {
            ImGui.beginCombo("Texture Type", getTextureType().toString());
        } else {
            ImGui.beginCombo("Texture Type", "NULL");
        }

        if (getAttachmentType() != null) {
            ImGui.beginCombo("Attachment Type", getAttachmentType().toString());

        } else {
            ImGui.beginCombo("Attachment Type", "NULL");
        }

        ImGui.spacing();

        ImGui.separatorText("Texture Information");
        ImGui.text("ID: " + id);

        float[] size_arr = new float[]{width, height};
        ImGui.inputFloat2("Size", size_arr, ImGuiInputTextFlags.ReadOnly);

        ImGui.imageWithBg(id, new ImVec2(128, 128));

        ImGui.unindent(16.0f);
    }

    public void setNearestFiltering() {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        unbind();
    }

    public int getId() {
        return this.id;
    }

    public AttachmentType getAttachmentType() {
        return attachment_type;
    }
    public TextureType getTextureType() {
        return texture_type;
    }
    public Cubemap.Face getFace() {
        return face;
    }
    public String getLocation() {return location; }


    public static Texture defaultTexture(int entity) {
//        if (defaultTexure == null) {
//            defaultTexure = new Texture("src/main/resources/textures/engine/defaultTexture.png");
//            return defaultTexure;
//        } else {
//            return defaultTexure;
//        }
        return new Texture("src/main/resources/textures/engine/defaultTexture.png", entity);
    }

    public static Texture defaultTexture() {
        if (defaultTexure == null) {
            defaultTexure = new Texture("src/main/resources/textures/engine/defaultTexture.png");
            defaultTexure.setNearestFiltering();
            return defaultTexure;
        } else {
            return defaultTexure;
        }
    }
}

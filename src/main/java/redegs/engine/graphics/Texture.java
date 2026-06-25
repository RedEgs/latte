package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSliderFlags;
import org.lwjgl.BufferUtils;
import org.w3c.dom.Text;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.asset.AssetManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;

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

@ComponentMeta(name = "Texture", category = "Assets", description = "A texture asset or generated texture.")
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
    private String generatedKind = null;
    private int minFilter = GL_LINEAR_MIPMAP_LINEAR;
    private int magFilter = GL_LINEAR;
    private int wrapS = GL_REPEAT;
    private int wrapT = GL_REPEAT;

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
    public Texture(int entity) {
        super(entity);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.IMAGE;
        attachment_type = null;

        setup = false;


    }

    static {
        ComponentRegistry.register(
                Texture.class,
                Texture::empty
        );
    }
    public Texture(int width, int height, AttachmentType attachment_type, int entity) {
        super(entity);
        name = "TextureComponent";

        this.attachment_type = attachment_type;
        createAttachment(width, height);
        setup = true;
    }
    public Texture(String path_to_texture, Cubemap.Face face) {
        super(-1);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.CUBEMAP;
        attachment_type = null;
        this.face = face;

        fromFile(path_to_texture, TextureType.CUBEMAP, face);
        setup = true;
    }
    public Texture(String path_to_texture) {
        super(-1);
        name = "TextureComponent";

        id = glGenTextures();
        texture_type = TextureType.IMAGE;
        attachment_type = null;

        fromFile(path_to_texture);

        setup = true;
    }
    public Texture(int width, int height, AttachmentType attachment_type) {
        super(-1);
        name = "TextureComponent";

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
        if (generatedKind != null) {
            o.addProperty("generated", generatedKind);
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
        o.addProperty("minFilter", minFilter);
        o.addProperty("magFilter", magFilter);
        o.addProperty("wrapS", wrapS);
        o.addProperty("wrapT", wrapT);

        try {
            if (location == null && generatedKind == null && image != null)
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
        if (data.get("generated") != null) {
            generatedKind = data.get("generated").getAsString();
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
        if (data.get("minFilter") != null) {
            minFilter = data.get("minFilter").getAsInt();
        }
        if (data.get("magFilter") != null) {
            magFilter = data.get("magFilter").getAsInt();
        }
        if (data.get("wrapS") != null) {
            wrapS = data.get("wrapS").getAsInt();
        }
        if (data.get("wrapT") != null) {
            wrapT = data.get("wrapT").getAsInt();
        }


        if (l != null) {
            location = l;
            fromFile(l);
        } else if ("black".equals(generatedKind)) {
            createSolidColor(0, 0, 0, 255);
        } else if ("empty".equals(generatedKind)) {
            setup = false;
        } else {
            String raw = data.get("data") != null ? data.get("data").getAsString() : null;
            if (raw != null && !raw.isBlank()) {
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
            glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
            glGenerateMipmap(GL_TEXTURE_2D);
            applyTextureParameters();
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

        if (type == TextureType.IMAGE && location != null) {
            AssetManager.register(location, this);
        }
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
            glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
            glGenerateMipmap(GL_TEXTURE_2D);
            applyTextureParameters();
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
        minFilter = GL_NEAREST;
        magFilter = GL_NEAREST;
        applyTextureParameters();
    }

    private void createSolidColor(int r, int g, int b, int a) {
        texture_type = TextureType.IMAGE;
        attachment_type = null;
        location = null;

        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        pixel.put((byte) r);
        pixel.put((byte) g);
        pixel.put((byte) b);
        pixel.put((byte) a);
        pixel.flip();

        bind();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
        applyTextureParameters();
        unbind();

        width = 1;
        height = 1;
        setup = true;
    }

    public void setLinearFiltering() {
        minFilter = GL_LINEAR_MIPMAP_LINEAR;
        magFilter = GL_LINEAR;
        applyTextureParameters();
    }

    public void setRepeatWrapping() {
        wrapS = GL_REPEAT;
        wrapT = GL_REPEAT;
        applyTextureParameters();
    }

    public void setClampWrapping() {
        wrapS = GL_CLAMP_TO_EDGE;
        wrapT = GL_CLAMP_TO_EDGE;
        applyTextureParameters();
    }

    private void applyTextureParameters() {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
        unbind();
    }

    public void drawAssetEditor() {
        ImGui.text("Location: " + getDisplayLocation());
        ImGui.text("ID: " + id);

        float[] size_arr = new float[]{width, height};
        ImGui.inputFloat2("Size", size_arr, ImGuiInputTextFlags.ReadOnly);

        ImGui.imageWithBg(id, new ImVec2(128, 128));
        ImGui.spacing();

        ImGui.text("Filtering");
        if (ImGui.button("Nearest")) {
            setNearestFiltering();
        }
        ImGui.sameLine();
        if (ImGui.button("Linear")) {
            setLinearFiltering();
        }

        ImGui.text("Wrapping");
        if (ImGui.button("Repeat")) {
            setRepeatWrapping();
        }
        ImGui.sameLine();
        if (ImGui.button("Clamp")) {
            setClampWrapping();
        }
    }

    public static Texture drawEditorSelectionPane() {
        Texture ret = null;
        float window_visible_x2 = ImGui.getCursorScreenPosX() + ImGui.getContentRegionAvailX();
        java.util.ArrayList<Texture> textures = new java.util.ArrayList<>(AssetManager.getAll(Texture.class));

        for (int i = 0; i < textures.size(); i++) {
            ImGui.pushID(i);

            Texture texture = textures.get(i);
            ImGui.imageWithBg(texture.getId(), new ImVec2(40, 40));
            if (ImGui.isItemClicked()) {
                ret = texture;
            }
            ImGui.setItemTooltip(texture.getDisplayName());

            float last_img_x2 = ImGui.getItemRectMaxX();
            float next_img_x2 = last_img_x2 + ImGui.getStyle().getItemSpacingX() + 40;
            if (i + 1 < textures.size() && next_img_x2 < window_visible_x2) {
                ImGui.sameLine();
            }

            ImGui.popID();
        }

        return ret;
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

    public String getAssetKey() {
        if (location != null && !location.isBlank()) {
            return location;
        }
        if (generatedKind != null && !generatedKind.isBlank()) {
            return generatedKind + "Texture";
        }

        return getDisplayName();
    }

    public String getDisplayName() {
        if (generatedKind != null && !generatedKind.isBlank()) {
            return generatedKind + "Texture";
        }
        if (location == null || location.isBlank()) {
            return "Texture " + id;
        }

        try {
            return java.nio.file.Path.of(location).getFileName().toString();
        } catch (Exception e) {
            return location;
        }
    }

    private String getDisplayLocation() {
        return location == null || location.isBlank() ? "(generated)" : location;
    }


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

    public static Texture empty(int entity) {
        Texture texture = new Texture(entity);
        texture.generatedKind = "empty";
        AssetManager.register("emptyTexture", texture);
        return texture;
    }

    public static Texture black(int entity) {
        Texture texture = new Texture(entity);
        texture.generatedKind = "black";
        texture.createSolidColor(0, 0, 0, 255);
        AssetManager.register("blackTexture", texture);
        return texture;
    }

    public static Texture blackTexture(int entity) {
        return black(entity);
    }

    public static Texture empty() {
        return empty(-1);
    }

    public static Texture black() {
        return black(-1);
    }
}

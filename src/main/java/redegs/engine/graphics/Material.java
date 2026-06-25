package redegs.engine.graphics;

import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import org.joml.Vector3f;
import org.w3c.dom.Text;
import redegs.engine.engine.gson.Save;
import redegs.engine.engine.system.asset.AssetManager;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

public class Material {
    public Vector3f ambient;
    public Texture diffuse;
    public Texture specular;
    public float shininess;
    protected String name;

    public Material(String name, Vector3f ambient, Texture diffuse, Texture specular, float shininess) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;

        this.name = name;
        AssetManager.register(name, this);
    }

    public JsonObject Save() {
        JsonObject o = new JsonObject();
        o.addProperty("name", name);
        o.add("ambient", Save.Vec3ToJson(ambient));
        if (diffuse != null) {
            o.add("diffuse", diffuse.Save());
        }
        if (specular != null) {
            o.add("specular", specular.Save());
        }
        o.addProperty("shininess", shininess);
        return o;
    }

    public void Load(JsonObject data) {
        if (data.has("name") && !data.get("name").isJsonNull()) {
            name = data.get("name").getAsString();
        }
        if (data.has("ambient") && !data.get("ambient").isJsonNull()) {
            ambient = Save.JsonToVec3(data.getAsJsonObject("ambient"));
        }
        diffuse = data.has("diffuse") && !data.get("diffuse").isJsonNull()
                ? Texture.fromJson(data.getAsJsonObject("diffuse"), 0)
                : null;
        specular = data.has("specular") && !data.get("specular").isJsonNull()
                ? Texture.fromJson(data.getAsJsonObject("specular"), 0)
                : null;
        if (data.has("shininess") && !data.get("shininess").isJsonNull()) {
            shininess = data.get("shininess").getAsFloat();
        }
    }

    public static Material fromJson(JsonObject data, int entity, String fallbackName) {
        String materialName = fallbackName;
        if (data.has("name") && !data.get("name").isJsonNull()) {
            materialName = data.get("name").getAsString();
        }

        Vector3f ambient = data.has("ambient") && !data.get("ambient").isJsonNull()
                ? Save.JsonToVec3(data.getAsJsonObject("ambient"))
                : new Vector3f(1, 1, 1);

        Texture diffuse = data.has("diffuse") && !data.get("diffuse").isJsonNull()
                ? Texture.fromJson(data.getAsJsonObject("diffuse"), entity)
                : null;
        Texture specular = data.has("specular") && !data.get("specular").isJsonNull()
                ? Texture.fromJson(data.getAsJsonObject("specular"), entity)
                : null;

        float shininess = data.has("shininess") && !data.get("shininess").isJsonNull()
                ? data.get("shininess").getAsFloat()
                : 32.0f;

        return new Material(materialName, ambient, diffuse, specular, shininess);
    }

    public void apply(Shader shader) {
        shader.setUniform3f("material.ambient", ambient);
        if (diffuse != null) {
            diffuse.use(0, shader, "material.diffuse");
        } else {
            Texture.defaultTexture().use(0, shader, "material.diffuse");
        }
        if (specular != null) {
            specular.use(1, shader, "material.specular");
        } else {
            Texture.defaultTexture().use(1, shader, "material.specular");
        }
        shader.setUniform1f("material.shininess", shininess);


    }

    public Material drawEditorImage(boolean selection_enabled) {
        Material ret = null;
        if (diffuse != null)  {
            ImGui.imageWithBg(diffuse.getId(), new ImVec2(40, 40));
            if (ImGui.beginItemTooltip()) {
                ImGui.text(getName());
                ImGui.spacing();
                ImGui.image(diffuse.getId(), new ImVec2(400, 400));
                ImGui.endTooltip();
            }
        } else {
            ImGui.imageWithBg(Texture.defaultTexture().getId(), new ImVec2(40, 40));
            if (ImGui.beginItemTooltip()) {
                ImGui.text(getName() + " (null)");
                ImGui.spacing();
                ImGui.image(Texture.defaultTexture().getId(), new ImVec2(400, 400));
                ImGui.endTooltip();
            }
        }

        if (selection_enabled) {
            ImGui.setNextWindowSize(ImGui.getStyle().getItemSpacingX() + (40*5), 350);
            if (ImGui.beginPopupContextWindow()) {
                ImGui.separatorText("Select material");
                Material selected = drawEditorSelectionPane();
                if (selected != null) {
                    ret = selected;
                }
                ImGui.endPopup();
            }
        }

        if (ret != null) {
            return ret;
        } else {
            return null;
        }


    }

    public Material drawEditorSelectionPane() {
        Material ret = null;
        float window_visible_x2 = ImGui.getCursorScreenPosX() + ImGui.getContentRegionAvailX();
        ArrayList<Material> materials = new ArrayList<>(AssetManager.getAll(Material.class));
        for (int i = 0; i < materials.size(); i ++) {
            ImGui.pushID(i);

            ImGui.pushStyleVar(ImGuiStyleVar.ImageBorderSize, 1.0f);
            Material material = materials.get(i);

            if (material.diffuse != null) {
                ImGui.imageWithBg(material.diffuse.getId(), new ImVec2(40, 40));
            } else {
                ImGui.imageWithBg(Texture.defaultTexture().getId(), new ImVec2(40, 40));
            }
            if (ImGui.isItemClicked()) {
                ret = material;
            }
            ImGui.setItemTooltip(material.getName());


            ImGui.popStyleVar();

            float last_img_x2 = ImGui.getItemRectMaxX();
            float next_img_x2 = last_img_x2 + ImGui.getStyle().getItemSpacingX() + 40;
            if (i + 1 < materials.size() && next_img_x2 < window_visible_x2) {
                ImGui.sameLine();
            }

            ImGui.popID();
        }
        if (ret != null) {
            return ret;
        } else {
            return null;
        }

    }

    public String getName() {
        return this.name;
    }

    public void drawEditorProperties(boolean textureSelectionEnabled) {
        ImGui.text("Material: " + getName());

        float[] ambientArr = new float[]{ambient.x, ambient.y, ambient.z};
        if (ImGui.colorEdit3("Ambient", ambientArr)) {
            ambient.set(ambientArr[0], ambientArr[1], ambientArr[2]);
        }

        float[] shininessArr = new float[]{shininess};
        if (ImGui.sliderFloat("Shininess", shininessArr, 0, 256)) {
            shininess = shininessArr[0];
        }

        if (textureSelectionEnabled) {
            diffuse = drawTextureSlot("Diffuse", diffuse);
            specular = drawTextureSlot("Specular", specular);
        }
    }

    private Texture drawTextureSlot(String label, Texture current) {
        Texture selected = current;
        ImGui.spacing();
        ImGui.text(label + ": " + getTextureDisplayName(current));

        if (current != null) {
            ImGui.imageWithBg(current.getId(), new ImVec2(40, 40));
            ImGui.sameLine();
        }

        if (ImGui.button("Select##" + label)) {
            ImGui.openPopup("SelectTexture##" + label);
        }
        ImGui.sameLine();
        if (ImGui.button("Clear##" + label)) {
            selected = null;
        }

        if (ImGui.beginPopup("SelectTexture##" + label)) {
            ImGui.separatorText("Select texture");
            Texture texture = Texture.drawEditorSelectionPane();
            if (texture != null) {
                selected = texture;
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }

        return selected;
    }

    private String getTextureDisplayName(Texture texture) {
        return texture == null ? "(none)" : texture.getDisplayName();
    }

    public static Material fromTexture(Texture texture) {
        String filename = texture.getDisplayName();
        Material m = new Material(filename, new Vector3f(1, 1, 1), texture, null, 0);
        return m;
    }

    public static Material fromTexture(Texture texture, Texture texture2) {
        String filename = texture.getDisplayName();
        Material m = new Material(filename, new Vector3f(1, 1, 1), texture, texture2, 0);
        return m;
    }

}

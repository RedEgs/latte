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

    public void Save() {
        JsonObject o = new JsonObject();
        o.add("ambient", Save.Vec3ToJson(ambient));
        o.add("diffuse", diffuse.Save());
        o.add("specular", specular.Save());
        o.addProperty("shininess", shininess);
    }

    public void Load(JsonObject data) {
        ambient = Save.JsonToVec3(data.getAsJsonObject("ambient"));
        diffuse.Load(data.getAsJsonObject("diffuse"));
        specular.Load(data.getAsJsonObject("specular"));
        shininess = data.getAsFloat();
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

    public static Material fromTexture(Texture texture) {
        String filename = Path.of(texture.location).getFileName().toString();
        Material m = new Material(filename, new Vector3f(1, 1, 1), texture, null, 0);
        return m;
    }

    public static Material fromTexture(Texture texture, Texture texture2) {
        String filename = Path.of(texture.location).getFileName().toString();
        Material m = new Material(filename, new Vector3f(1, 1, 1), texture, texture2, 0);
        return m;
    }

}

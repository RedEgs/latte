package redegs.engine.graphics;

import org.joml.Vector3f;

public class Material {
    public final Vector3f ambient;
    public final Texture diffuse;
    public final Texture specular;
    public final float shininess;

    public Material(Vector3f ambient, Texture diffuse, Texture specular, float shininess) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }
    public void apply(Shader shader) {
        shader.setUniform3f("material.ambient", ambient);
        if (diffuse != null) {
            diffuse.use(0, shader, "material.diffuse");
            diffuse.use(1, shader, "material.specular");
        }
        shader.setUniform1f("material.shininess", shininess);
    }

    public static Material fromTexture(Texture texture) {
        Material m = new Material(new Vector3f(1, 1, 1), texture, null, 0);
        return m;
    }

}

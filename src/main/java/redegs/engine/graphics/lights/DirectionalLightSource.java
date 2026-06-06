package redegs.engine.graphics.lights;

import org.joml.Vector3f;

public class DirectionalLightSource {
    public Vector3f direction;
    public Vector3f ambient;
    public Vector3f diffuse;
    public Vector3f specular;

    public DirectionalLightSource(Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.direction = direction;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }
}

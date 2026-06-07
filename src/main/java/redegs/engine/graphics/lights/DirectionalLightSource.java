package redegs.engine.graphics.lights;

import org.joml.Matrix4f;
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

    public static Matrix4f getLightMatrix() {
        return new Matrix4f().ortho(-50f, 50f, -50f, 50f, 0.1f, 100f);
    }

    public static Matrix4f calculateLightSpaceMatrix(Vector3f directionOrPosition, Matrix4f lightMatrix) {
        Vector3f dir = directionOrPosition;
        Matrix4f lightView = new Matrix4f().lookAt(dir, new Vector3f(0), new Vector3f(0, 1, 0));
        return new Matrix4f(lightMatrix).mul(lightView);
    }
}

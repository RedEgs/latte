package redegs.engine.graphics.lights;

import org.joml.Vector3f;
import redegs.engine.graphics.LightSource;
import redegs.engine.graphics.Shader;

import java.util.ArrayList;

public class PointLightSource extends LightSource {
    public final float radius;

    public PointLightSource(Vector3f position, Vector3f color, float intensity, float radius, int entity) {
        super(position, color, intensity, entity);
        this.name = "PointLightSourceComponent";
        this.radius = radius;
    }
    public PointLightSource(Vector3f position, Vector3f color, float intensity, float radius) {
        super(position, color, intensity);
        this.name = "PointLightSourceComponent";
        this.radius = radius;
    }

}

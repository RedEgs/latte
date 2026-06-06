package redegs.engine.graphics;

import org.joml.Vector3f;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.ArrayList;

public class LightSource {
    public final Vector3f position;
    public final Vector3f color;
    public final float intensity;

    public LightSource(Vector3f position, Vector3f color, float intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }
}


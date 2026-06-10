package redegs.engine.graphics;

import org.joml.Vector3f;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.ArrayList;

public class LightSource extends Component {
    public Vector3f position;
    public final Vector3f color;
    public final float intensity;

    public LightSource(Vector3f position, Vector3f color, float intensity, int entity) {
        super(entity);
        name = "LightSourceComponent";

        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }

    public LightSource(Vector3f position, Vector3f color, float intensity) {
        super(EntitySceneManager.getInstance().createEntity());
        name = "LightSourceComponent";

        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }
}


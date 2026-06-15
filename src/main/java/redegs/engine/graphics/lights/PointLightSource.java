package redegs.engine.graphics.lights;

import imgui.ImGui;
import org.joml.Vector3f;
import redegs.engine.graphics.LightSource;

public class PointLightSource extends LightSource {
    public float radius;

    private final float[] radiusSlider = new float[1];

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

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        radiusSlider[0] = radius;
        if (ImGui.sliderFloat("Radius", radiusSlider, 0, 100)) {
            radius = radiusSlider[0];
        }
    }
}
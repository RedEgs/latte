package redegs.engine.graphics.lights;

import imgui.ImGui;
import org.joml.Vector3f;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;
import redegs.engine.graphics.LightSource;
import redegs.engine.graphics.Transform;

@ComponentMeta(name = "Point Light", category = "Lighting", description = "A positional light source.")
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

    static {
        ComponentRegistry.register(
                PointLightSource.class,
                () -> new PointLightSource(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), 1.0f, 1.0f)
        );
    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        ImGui.indent(16.0f);

        radiusSlider[0] = radius;
        if (ImGui.sliderFloat("Radius", radiusSlider, 0, 100)) {
            radius = radiusSlider[0];
        }

        ImGui.unindent(16.0f);
    }

}
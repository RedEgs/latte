package redegs.engine.graphics;

import org.joml.Matrix4f;
import redegs.engine.engine.Camera;

import java.util.List;

public class RenderContext {
    public Camera camera;

    public double delta_time;
    public double elapsed_time;
    public Long frame_index;

    public Integer width, height;

    public List<Model> models;
}

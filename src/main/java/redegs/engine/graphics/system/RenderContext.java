package redegs.engine.graphics.system;

import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.buffers.FrameBuffer;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.List;

public class RenderContext {
    public Camera camera;

    public double delta_time;
    public double elapsed_time;
    public Long frame_index;

    public Integer width, height;

    public List<Model> models;
    public List<PointLightSource> lights;
    public DirectionalLightSource dir_light;

    public FrameBuffer gbuffer;
    public FrameBuffer shadowmap;
}

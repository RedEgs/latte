package redegs.engine.graphics.system.render;

import redegs.engine.engine.entities.Billboard;
import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Cubemap;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.buffers.FrameBuffer;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.ArrayList;
import java.util.List;

public class RenderContext {
    public Camera camera;

    public double delta_time;
    public double elapsed_time;
    public Long frame_index;

    public Integer width, height;

    public List<Model> models = new ArrayList<>();
    public List<Billboard> billboards = new ArrayList<>();
    public List<PointLightSource> lights = new ArrayList<>();
    public DirectionalLightSource dir_light;

    public FrameBuffer gbuffer;
    public FrameBuffer shadowmap;

    public Cubemap skybox;
}

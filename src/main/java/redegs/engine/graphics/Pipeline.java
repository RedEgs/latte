package redegs.engine.graphics;

import org.joml.Matrix4f;
import redegs.Engine;
import redegs.engine.engine.Camera;
import redegs.engine.graphics.lights.PointLightSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pipeline {
    private final String name = "GenericPipeline";
    private final RenderContext render_context;
    private final List<RenderPass> render_passes;

    public Pipeline() {
        this.render_context = new RenderContext();
        this.render_passes = new ArrayList<>();

        render_context.width = Engine.getScreenWidth();
        render_context.height = Engine.getScreenHeight();

        render_context.camera = new Camera(render_context.width, render_context.height);
        render_context.models = new ArrayList<Model>();
        render_context.lights = new ArrayList<PointLightSource>();
    }

    public void Execute() {
        for (RenderPass p : render_passes) {
            onPrePass(p.name);
            p.Execute(render_context);
            onPostPass(p.name);
        }
    }

    protected void onPrePass(String render_pass_name) {}
    protected void onPostPass(String render_pass_name) {}


    public <T extends RenderPass> void NewPass(Class<T> pass) {
        try {
            T p = pass.getDeclaredConstructor(RenderContext.class).newInstance(render_context);
            render_passes.add(p);

        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }

    public RenderContext getRenderContext() {
        return render_context;
    }
}

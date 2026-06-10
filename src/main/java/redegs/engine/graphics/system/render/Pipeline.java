package redegs.engine.graphics.system.render;

import redegs.Engine;
import redegs.engine.engine.entities.Billboard;
import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.PointLightSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private final String name = "GenericPipeline";
    private RenderContext render_context = new RenderContext();
    private List<RenderPass> render_passes = new ArrayList<>();
    protected boolean builtPipeline = false;

    public Pipeline() {
    }

    public void BuildPipeline() {
        render_context.camera = new Camera(Engine.getScreenWidth(), Engine.getScreenHeight());
        render_context.width = Engine.getScreenWidth();
        render_context.height = Engine.getScreenHeight();

        render_context.models = new ArrayList<Model>();
        render_context.billboards = new ArrayList<Billboard>();
        render_context.lights = new ArrayList<PointLightSource>();

        builtPipeline = true;
    }

    public void Execute() {
        if (!builtPipeline) return;
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
    public void setCamera(Camera c) {
        render_context.camera = c;
    }
}

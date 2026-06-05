package redegs.engine.graphics;

import org.joml.Matrix4f;
import redegs.engine.engine.Camera;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pipeline {
    private String name;
    private final RenderContext render_context;
    private final List<RenderPass> render_passes;

    public Pipeline(String name, int width, int height) {
        this.name = name;
        this.render_context = new RenderContext();
        this.render_passes = new ArrayList<>();

        render_context.width = width;
        render_context.height = height;

        render_context.camera = new Camera(width, height);
        render_context.models = new ArrayList<Model>();
    }

    public void Execute() {
        for (int i = 0; i < render_passes.size(); i++) {
            RenderPass p = render_passes.get(i);
            p.Execute(render_context);

        }
    }

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

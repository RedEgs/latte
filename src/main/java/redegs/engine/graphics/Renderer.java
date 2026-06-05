package redegs.engine.graphics;

import redegs.engine.engine.Camera;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Renderer {
    private Pipeline pipeline;

    public Renderer(int width, int height) {
        pipeline = new Pipeline("main", width, height);
    }

    public static Renderer unlitRenderer(int width, int height) {
        Renderer r = new Renderer(width, height);
        r.NewPass(UnlitPass.class);

        return r;
    }

    public void Execute(double delta_time, double elapsed_time) {
        pipeline.getRenderContext().delta_time = delta_time;
        pipeline.getRenderContext().elapsed_time = elapsed_time;
        pipeline.Execute();
    }









    public <T extends RenderPass> void NewPass(Class<T> pass) {
        pipeline.NewPass(pass);
    }








    public void SubmitModel(Model model) {
        pipeline.getRenderContext().models.add(model);
    }
    public void SubmitModels(List<Model> models) {
        pipeline.getRenderContext().models.addAll(models);
    }
    public void ClearModels() {pipeline.getRenderContext().models.clear(); }
    public List<Model> PopModels() {
        List<Model> models = new ArrayList<>( pipeline.getRenderContext().models);
        pipeline.getRenderContext().models.clear();
        return models;
    }
    public List<Model> getModels() {
        return pipeline.getRenderContext().models;
    }

    public <T extends Camera> void setCamera(T camera) {
        this.pipeline.getRenderContext().camera = camera;
    }
    public Camera getCamera() {
        return pipeline.getRenderContext().camera;
    }



}


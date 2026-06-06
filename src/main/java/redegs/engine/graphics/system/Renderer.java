package redegs.engine.graphics.system;

import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class Renderer<T extends Pipeline> {
    protected T pipeline;


    public Renderer(Supplier<T> supplier) {
        this.pipeline = supplier.get();
    }

//    public static Renderer unlitRenderer(int width, int height) {
//        Renderer r = new Renderer(width, height);
//        r.NewPass(UnlitPass.class);
//
//        return r;
//    }
//    public static Renderer litRenderer(int width, int height) {
//        Renderer r = new Renderer(width, height);
//        r.NewPass(LitPass.class);
//
//        return r;
//    }

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

    public void SubmitDirectionalLight(DirectionalLightSource light) {
        pipeline.getRenderContext().dir_light = light;
    }

    public void SubmitLight(PointLightSource light) {
        pipeline.getRenderContext().lights.add(light);
    }
    public void SubmitLights(List<PointLightSource> lights) {
        pipeline.getRenderContext().lights.addAll(lights);
    }
    public void ClearLights() {
        pipeline.getRenderContext().lights.clear();
    }
    public List<PointLightSource> getLights() {
        return pipeline.getRenderContext().lights;
    }

    public <T extends Camera> void setCamera(T camera) {
        this.pipeline.getRenderContext().camera = camera;
    }
    public Camera getCamera() {
        return pipeline.getRenderContext().camera;
    }



}


package redegs.engine.graphics.system.render;

import redegs.engine.engine.components.Billboard;
import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Cubemap;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class Renderer<T extends Pipeline> {
    protected T pipeline;
    protected boolean debugRendering = false;
    protected int debugLevel = 0;

    public Renderer(Supplier<T> supplier) {
        this.pipeline = supplier.get();
    }
    public void Execute(double delta_time, double elapsed_time) {
        if (!pipeline.builtPipeline) return;
        pipeline.getRenderContext().delta_time = delta_time;
        pipeline.getRenderContext().elapsed_time = elapsed_time;
        pipeline.Execute();
    }
    public void BuildPipeline() {
        pipeline.BuildPipeline();
    }


    public <T extends RenderPass> void newPass(Class<T> pass) {
        pipeline.NewPass(pass);
    }

    public void submitModel(Model model) {
        pipeline.getRenderContext().models.add(model);
    }
    public void submitModels(List<Model> models) {
        pipeline.getRenderContext().models.addAll(models);
    }
    public void clearModels() {pipeline.getRenderContext().models.clear(); }
    public List<Model> popModels() {
        List<Model> models = new ArrayList<>( pipeline.getRenderContext().models);
        pipeline.getRenderContext().models.clear();
        return models;
    }
    public List<Model> getModels() {
        return pipeline.getRenderContext().models;
    }

    public void submitBillboard(Billboard Billboard) {
        pipeline.getRenderContext().billboards.add(Billboard);
    }
    public void submitBillboards(List<Billboard> Billboards) {
        pipeline.getRenderContext().billboards.addAll(Billboards);
    }
    public void clearBillboards() {pipeline.getRenderContext().billboards.clear(); }
    public List<Billboard> popBillboards() {
        List<Billboard> Billboards = new ArrayList<>( pipeline.getRenderContext().billboards);
        pipeline.getRenderContext().billboards.clear();
        return Billboards;
    }
    public List<Billboard> getBillboards() {
        return pipeline.getRenderContext().billboards;
    }


    public void submitDirectionalLight(DirectionalLightSource light) {
        pipeline.getRenderContext().dir_light = light;
    }
    public void submitLight(PointLightSource light) {
        pipeline.getRenderContext().lights.add(light);
    }
    public void submitLights(List<PointLightSource> lights) {
        pipeline.getRenderContext().lights.addAll(lights);
    }
    public void clearLights() {
        pipeline.getRenderContext().lights.clear();
    }
    public List<PointLightSource> getLights() {
        return pipeline.getRenderContext().lights;
    }

    public void submitSkybox(Cubemap cubemap) {
        pipeline.getRenderContext().skybox = cubemap;
    }
    public void clearSkybox() {
        pipeline.getRenderContext().skybox = null;
    }

    public <T extends Camera> void submitCamera(T camera) {
        this.pipeline.getRenderContext().camera = camera;
    }
    public Camera getCamera() {
        return pipeline.getRenderContext().camera;
    }

    public void selectModel(Model model) {
        this.pipeline.getRenderContext().selected_model = model;
    }

    public void resize(int width, int height) {
        this.pipeline.setSize(width, height);
    }


    public void setDebugRendering(boolean value) {
        this.debugRendering = value;
        pipeline.getRenderContext().debugRendering = this.debugRendering;
    }

    public boolean getDebugRendering() {
        return this.debugRendering;
    }

    public void toggleDebugRendering() {
        if (!this.debugRendering) {
            this.debugRendering = true;
        } else {
            this.debugRendering = false;
        }

        pipeline.getRenderContext().debugRendering = this.debugRendering;
    }

    public void setDebugLevel(int val) {
        this.debugLevel = val;
    }

    public int getDebugLevel() {
        return this.debugLevel;
    }
}

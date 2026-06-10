package redegs.engine.engine.system.scene;

import redegs.engine.engine.entities.Billboard;
import redegs.engine.engine.entities.ControllableCamera;
import redegs.engine.graphics.Camera;
import redegs.engine.graphics.Cubemap;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.lights.PointLightSource;
import redegs.engine.graphics.system.render.Renderer;

public final class SceneRenderSync {
    private final Renderer renderer;

    public SceneRenderSync(Renderer renderer) { this.renderer = renderer; }

    public void sync(Scene scene) {
        renderer.clearModels();
        renderer.clearLights();
        renderer.clearBillboards();

        var skybox = scene.getStore(Cubemap.class).toList();
        if (!skybox.isEmpty()) renderer.submitSkybox(skybox.get(0));

        var sun = scene.getStore(DirectionalLightSource.class).toList();
        if (!sun.isEmpty()) renderer.submitDirectionalLight(sun.get(0));

        renderer.submitLights(scene.getStore(PointLightSource.class).toList());
        renderer.submitModels(scene.getStore(Model.class).toList());
        renderer.submitBillboards(scene.getStore(Billboard.class).toList());


        if (!scene.getStore(ControllableCamera.class).toList().isEmpty()) {
            renderer.submitCamera(scene.getStore(ControllableCamera.class).toList().getFirst());
            scene.camera = scene.getStore(ControllableCamera.class).toList().getFirst();
        }

    }
}
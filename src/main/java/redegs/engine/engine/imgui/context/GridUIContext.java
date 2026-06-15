package redegs.engine.engine.imgui.context;

import imgui.extension.imguizmo.ImGuizmo;
import org.joml.Matrix4f;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.system.EntitySceneManager;

public class GridUIContext extends UIContext {

    EntitySceneManager esm = EntitySceneManager.getInstance();


    private final float[] viewMatrix = new float[16];
    private final float[] projMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    @Override
    public void Draw() {
        super.Draw();

        esm.GetScene().camera.getView().get(viewMatrix);
        esm.GetScene().camera.getProjection().get(projMatrix);
        Matrix4f mat = new Matrix4f().identity();
        mat.get(modelMatrix);

        ImGuizmo.drawGrid(viewMatrix, projMatrix, modelMatrix, 1000f);

    }
}

package redegs.engine.graphics.passes;

import redegs.Engine;
import redegs.engine.engine.imgui.UIManager;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import static org.lwjgl.opengl.GL11C.*;

public class ImGuiPass extends RenderPass {
    private Boolean ready = false;
    private UIManager uim;

    public ImGuiPass(RenderContext render_context) {
        super(render_context);
        name = "ImGuiPass";
        uim = Engine.getUIManager();
        ready = true;
    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);



        if (ready) {
            uim.Execute();
        }


    }
}

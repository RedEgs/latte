package redegs.engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class RenderPass {
    protected String name;

    public RenderPass(RenderContext render_context) {}
    public void Execute(RenderContext render_context) {}

    protected void DrawGeometry(RenderContext render_context, Shader shader) {
        for (int i = 0; i < render_context.models.size(); i++) {
            Model m = render_context.models.get(i);
            m.Draw(shader);
        }
    }
    protected void Clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

    }

}

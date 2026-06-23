package redegs.engine.graphics.passes;

import org.joml.Vector3f;
import redegs.engine.engine.components.Billboard;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;

public class BillboardPass extends RenderPass {
    private Shader shader = new Shader(Shader.FragmentShader_billboard, Shader.VertexShader_billboard);

    public BillboardPass(RenderContext render_context) {
        super(render_context);
        name = "BillboardPass";
    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDepthMask(false);  // don't write depth

        shader.use();

        shader.setUniformMat4("proj", render_context.camera.getProjection());
        shader.setUniformMat4("view", render_context.camera.getView());
        shader.setUniform3f("c_pos", render_context.camera.getPosition());
        shader.setUniform1f("size", 1.0f);

        DrawGeometry(render_context, shader);

        glDisable(GL_BLEND);
        glDepthMask(true);

    }

    @Override
    protected void DrawGeometry(RenderContext render_context, Shader shader) {

        List<Billboard> sorted = new ArrayList<>(render_context.billboards);

        sorted.sort((a, b) -> {
            float distA = new Vector3f(render_context.camera.getPosition())
                    .sub(a.getPosition())
                    .lengthSquared();

            float distB = new Vector3f(render_context.camera.getPosition())
                    .sub(b.getPosition())
                    .lengthSquared();

            return Float.compare(distA, distB); // farthest first
        });

        for (Billboard billboard : sorted) {
            if (billboard.getOntop()) {
                glDisable(GL_DEPTH);
                billboard.Draw(shader);
                glEnable(GL_DEPTH);
            } else{

                billboard.Draw(shader);
            }

        }
    }
}

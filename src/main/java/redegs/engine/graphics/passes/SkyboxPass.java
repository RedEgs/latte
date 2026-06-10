package redegs.engine.graphics.passes;

import redegs.engine.graphics.MeshPrimitives;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import static org.lwjgl.opengl.GL11C.*;

public class SkyboxPass extends RenderPass {
    Shader shader = new Shader(Shader.FragmentShader_cubemap, Shader.VertexShader_cubemap);
    Model cube = Model.fromMesh(MeshPrimitives.skyboxCube());

    public SkyboxPass(RenderContext render_context) {
        super(render_context);
        name = "SkyboxPass";

    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        glDepthMask(false);
        glDepthFunc(GL_LEQUAL);




        shader.use();
        shader.setUniform1f("skybox", render_context.skybox.getId());
        shader.setUniformMat4("proj", render_context.camera.getProjection());
        shader.setUniformMat4("view", render_context.camera.getStaticViewMatrix());
        cube.Draw(shader);

        glDepthMask(true);
        glDepthFunc(GL_LESS); // set depth function back to default
    }

    @Override
    protected void DrawGeometry(RenderContext render_context, Shader shader) {
        super.DrawGeometry(render_context, shader);

        cube.Draw(shader);
    }

}

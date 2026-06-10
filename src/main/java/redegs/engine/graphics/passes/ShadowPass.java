package redegs.engine.graphics.passes;

import redegs.engine.graphics.Shader;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import static org.lwjgl.opengl.GL11C.*;

public class ShadowPass extends RenderPass {
    Shader shader = new Shader(Shader.FragmentShader_shadowDepth, Shader.VertexShader_shadowDepth);

    public ShadowPass(RenderContext render_context) {
        super(render_context);
        name = "GeometryPass";
    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        render_context.shadowmap.bind();
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.use();
        shader.setUniformMat4("lightSpaceMatrix", DirectionalLightSource.calculateLightSpaceMatrix(render_context.dir_light.direction, DirectionalLightSource.getLightMatrix()));
        DrawGeometry(render_context, shader);

        render_context.shadowmap.unbind();
    }
}

package redegs.engine.graphics.passes;

import org.joml.Vector3f;
import redegs.engine.engine.components.BoundingBox;
import redegs.engine.graphics.Mesh;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

public class BoundingBoxPass extends RenderPass {
    public static String FragmentShader_camera = """
    #version 330 core
    out vec4 FragColor;
        
    in vec3 color;
    in vec2 uv;
    uniform vec3 col;

    void main()
    {
        FragColor = vec4(col, 1.0f);
    }
    """;

    private Shader shader = new Shader(FragmentShader_camera, Shader.VertexShader_camera);
    public BoundingBoxPass(RenderContext render_context) {
        super(render_context);
        name = "BoundingBoxPass";
    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);
        name = "BoundingBoxPass";

        shader.use();

        if (render_context.selected_model != null) {
            shader.setUniformMat4("model",  render_context.selected_model.getModelMatrix());
            shader.setUniform3f("col", new Vector3f(1, 1, 0));
            BoundingBox bb = render_context.selected_model.getBoundingBox();
            if (bb == null) {
                render_context.selected_model.computeBoundingBox();
            } else {
                Mesh mesh = bb.toWireframeMesh();
                mesh.DrawLines(shader);
            }

        }


    }
}

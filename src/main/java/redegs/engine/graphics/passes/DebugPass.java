package redegs.engine.graphics.passes;

import redegs.engine.graphics.Model;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import static org.lwjgl.opengl.GL11C.*;

public class DebugPass extends RenderPass {
    public static String VertexShader_debug = """
    #version 330 core
    #extension GL_NV_shader_buffer_load : enable
    
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aNormal;
    layout (location = 2) in vec2 aUV;
       
    layout (std140) uniform CameraInfo {
        mat4 proj;
        mat4 view;
    };
    
    uniform mat4 model;
    
    out vec3 color;
    out vec2 uv;
    
    void main()
    {    
        gl_Position = proj * view * model * vec4(aPos, 1.0);
        
        color = vec3(1.0f);
        uv = aUV;       
    }
    """;

    public static String FragmentShader_debug = """
    #version 330 core
    out vec4 FragColor;
        
    in vec3 color;
    in vec2 uv;
    
    void main()
    {
        FragColor = vec4(color, .5f);
    }
    """;

    private final Shader shader = new Shader(DebugPass.FragmentShader_debug, DebugPass.VertexShader_debug);
    private Model highlighted_model;


    public DebugPass(RenderContext render_context) {
        super(render_context);
    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);
        glClear(GL_STENCIL_BUFFER_BIT);

        if (render_context.selected_model != null)
            highlighted_model = render_context.selected_model;
        if (highlighted_model == null) return;

        glEnable(GL_STENCIL_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilMask(0xFF);

        shader.use();
        highlighted_model.DrawNoMaterials(shader);

        glStencilMask(0xFF);
        glStencilFunc(GL_ALWAYS, 0, 0xFF);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_BLEND);

        glCullFace(GL_BACK);

    }

    public void setModel(Model model) {
        highlighted_model = model;
    }
}

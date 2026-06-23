package redegs.engine.graphics.passes;

import org.joml.Vector3f;
import redegs.engine.engine.components.Billboard;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.lights.PointLightSource;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private Shader billboard_shader = new Shader(Shader.FragmentShader_billboard, Shader.VertexShader_billboard);

    private final HashMap<PointLightSource, Billboard> billboards = new HashMap<>();
    private Model highlighted_model;


    public DebugPass(RenderContext render_context) {
        super(render_context);
    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        if (render_context.debugRendering) return;
        glClear(GL_STENCIL_BUFFER_BIT);

//        if (billboards.values().size() != render_context.lights.size()) {
//            System.out.println("Building");
//            billboards.clear();
//            for (PointLightSource l: render_context.lights) {
//                Billboard b = new Billboard("src/main/resources/icons/point-light.png");
//                billboards.put(l, b);
//            }
//        } else {
//            System.out.println("Drawing");
//            glEnable(GL_BLEND);
//            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//
//            glDepthMask(false);  // don't write depth
//
//            billboard_shader.use();
//
//            billboard_shader.setUniformMat4("proj", render_context.camera.getProjection());
//            billboard_shader.setUniformMat4("view", render_context.camera.getView());
//            billboard_shader.setUniform3f("c_pos", render_context.camera.getPosition());
//            billboard_shader.setUniform1f("size", 1.0f);
//
//            DrawBillboards(render_context, billboard_shader);
//
//            glDisable(GL_BLEND);
//            glDepthMask(true);
//        }

        if (render_context.selected_model != null)
            highlighted_model = render_context.selected_model;
        else {
            return;
        };

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


    protected void DrawBillboards(RenderContext render_context, Shader shader) {

        List<Billboard> sorted = new ArrayList<>(billboards.values());

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

    public void setModel(Model model) {
        highlighted_model = model;
    }
}

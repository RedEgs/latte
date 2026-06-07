package redegs.engine.graphics.system;

import redegs.engine.graphics.Model;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.lights.PointLightSource;

import static org.lwjgl.opengl.GL11.*;

public class RenderPass {
    protected String name;

    public RenderPass(RenderContext render_context) {}
    public void Execute(RenderContext render_context) {}

    protected void DrawGeometry(RenderContext render_context, Shader shader) {
        for (Model model : render_context.models) {
            model.Draw(shader);
        }
    }
    protected void UploadLights(RenderContext render_context, Shader shader) {
        shader.setUniform1i("light_count", render_context.models.size());
        for (int i = 0; i < render_context.lights.size(); i++) {
            PointLightSource light =  render_context.lights.get(i);
            String name = "lights[" + i + "]";

            shader.setUniform3f(name + ".position" , light.position);
            shader.setUniform3f(name + ".color", light.color);
            shader.setUniform1f(name + ".radius", light.radius);
            shader.setUniform1f(name + ".intensity", light.intensity);
        }
        UploadDirectionalLight(render_context, shader);
    }

    protected void UploadDirectionalLight(RenderContext render_context, Shader shader) {
        if (render_context.dir_light != null) {
            shader.setUniform3f("dir_light.direction", render_context.dir_light.direction);
            shader.setUniform3f("dir_light.ambient", render_context.dir_light.ambient);
            shader.setUniform3f("dir_light.diffuse", render_context.dir_light.diffuse);
            shader.setUniform3f("dir_light.specular", render_context.dir_light.specular);
        }
    }

    protected void Clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

    }

}

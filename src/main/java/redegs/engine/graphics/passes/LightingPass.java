package redegs.engine.graphics.passes;

import org.lwjgl.system.MemoryStack;
import redegs.Engine;
import redegs.engine.graphics.Mesh;
import redegs.engine.graphics.MeshPrimitives;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.buffers.UniformBuffer;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;

public class LightingPass  extends RenderPass {
    Shader shader = new Shader(Shader.FragmentShader_lighting, Shader.VertexShader_lighting);
    Mesh quad = MeshPrimitives.quadScreen();

    UniformBuffer ubo;

    public LightingPass (RenderContext render_context) {
        super(render_context);

        name = "LightingPass";
        ubo = new UniformBuffer("CameraInfo", shader, 128, 0);
        updateCameraUniformBuffer(render_context);

    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        // Clear the default framebuffer (screen)
        glViewport(0, 0, Engine.getScreenWidth(), Engine.getScreenHeight());
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        updateCameraUniformBuffer(render_context);

        shader.use();
        render_context.gbuffer.getColorTexture(0).use(0, shader, "gPosition");
        render_context.gbuffer.getColorTexture(1).use(1, shader, "gNormal");
        render_context.gbuffer.getColorTexture(2).use(2, shader, "gAlbedoSpec");
        render_context.gbuffer.getColorTexture(3).use(3, shader, "gFragPosLightSpace");
        render_context.shadowmap.getDepthTexture().use(4, shader, "gShadowmap");
        UploadLights(render_context, shader);

        quad.Draw(shader);

        render_context.gbuffer.blitToDefaultFramebuffer();

    }

    private void updateCameraUniformBuffer(RenderContext render_context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Use FloatBuffer for matrix data, not ByteBuffer
            FloatBuffer projBuffer = stack.mallocFloat(16);
            FloatBuffer viewBuffer = stack.mallocFloat(16);
            FloatBuffer combinedBuffer = stack.mallocFloat(32); // 16 + 16 floats

            render_context.camera.getProjection().get(projBuffer);
            render_context.camera.getView().get(viewBuffer);

            // Combine both matrices
            combinedBuffer.put(projBuffer);
            combinedBuffer.put(viewBuffer);
            combinedBuffer.flip(); // IMPORTANT: flip before sending to OpenGL

            // Now convert to ByteBuffer for the update method
            // This assumes your update() method expects ByteBuffer
            ByteBuffer byteBuffer = stack.malloc(128);
            for (int i = 0; i < 32; i++) {
                byteBuffer.putFloat(combinedBuffer.get(i));
            }
            byteBuffer.flip();

            ubo.update(byteBuffer);
        }
    }
}

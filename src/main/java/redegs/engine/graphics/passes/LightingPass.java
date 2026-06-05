package redegs.engine.graphics.passes;

import org.lwjgl.system.MemoryStack;
import redegs.engine.graphics.*;
import redegs.engine.graphics.buffers.FrameBuffer;
import redegs.engine.graphics.buffers.UniformBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;

public class LightingPass  extends RenderPass {
    Shader shader = new Shader(Shader.FragmentShader_lighting, Shader.VertexShader_lighting);
    Mesh quad = Mesh.quadScreen();

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
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        updateCameraUniformBuffer(render_context);

        shader.use();
        render_context.gbuffer.getColorTexture(0).use(0, shader, "gPosition");
        render_context.gbuffer.getColorTexture(1).use(1, shader, "gNormal");
        render_context.gbuffer.getColorTexture(2).use(2, shader, "gAlbedoSpec");

        quad.Draw(shader);
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

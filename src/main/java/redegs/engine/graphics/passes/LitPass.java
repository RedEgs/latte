package redegs.engine.graphics.passes;

import org.lwjgl.system.MemoryStack;
import redegs.engine.graphics.*;
import redegs.engine.graphics.buffers.UniformBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class LitPass extends RenderPass {
    Shader shader = new Shader(Shader.FragmentShader_lit, Shader.VertexShader_lit);
    UniformBuffer ubo;

    public LitPass(RenderContext render_context) {
        super(render_context);

        name = "LitPass";
        ubo = new UniformBuffer("CameraInfo", shader, 128, 0);
        updateCameraUniformBuffer(render_context);

    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        updateCameraUniformBuffer(render_context);

        shader.use();
        shader.setUniform3f("lightPos", 0, -2, 0);
        DrawGeometry(render_context, shader);
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

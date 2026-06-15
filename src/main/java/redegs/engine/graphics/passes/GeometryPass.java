package redegs.engine.graphics.passes;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import redegs.Engine;
import redegs.engine.engine.imgui.UIContext;
import redegs.engine.engine.imgui.context.GridUIContext;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.Shader;
import redegs.engine.graphics.buffers.UniformBuffer;
import redegs.engine.graphics.lights.DirectionalLightSource;
import redegs.engine.graphics.system.render.RenderContext;
import redegs.engine.graphics.system.render.RenderPass;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;

public class GeometryPass extends RenderPass {
    Shader shader = new Shader(Shader.FragmentShader_geometry, Shader.VertexShader_geometry);

    UniformBuffer ubo;

    public GeometryPass(RenderContext render_context) {
        super(render_context);

        name = "GeometryPass";
        ubo = new UniformBuffer("CameraInfo", shader, 128, 0);
        updateCameraUniformBuffer(render_context);

    }

    @Override
    public void Execute(RenderContext render_context) {
        super.Execute(render_context);

        render_context.gbuffer.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);

        updateCameraUniformBuffer(render_context);



        shader.use();
        shader.setUniform3f("view_pos", render_context.camera.getPosition());
        shader.setUniformMat4("lightSpaceMatrix", DirectionalLightSource.calculateLightSpaceMatrix(render_context.dir_light.direction, DirectionalLightSource.getLightMatrix()));

        DrawGeometry(render_context, shader);

        render_context.gbuffer.unbind();


    }

    @Override
    protected void DrawGeometry(RenderContext render_context, Shader shader) {
        for (Model model : render_context.models) {
            // Get the object's model matrix
            Matrix4f modelMatrix = model.getModelMatrix();

            // Calculate normal matrix from model matrix
            Matrix3f normalMatrix = new Matrix3f();
            modelMatrix.get3x3(normalMatrix);
            normalMatrix.invert().transpose();

            // Upload to shader
            shader.setUniformMat3("normalMatrix", normalMatrix);
            shader.setUniformMat4("model", modelMatrix); // You'll need this too

            // Draw the mesh
            if (model.equals(render_context.selected_model)) {
                glStencilFunc(GL_ALWAYS, 1, 0xFF);
                glStencilMask(0xFF);
            } else {
                glStencilMask(0x00);
            }
            model.Draw(shader);
        }
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

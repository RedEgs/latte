package redegs.engine.graphics.buffers;

import redegs.engine.graphics.Shader;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBUniformBufferObject.*;
import static org.lwjgl.opengl.GL15C.*;

public class UniformBuffer {
    private int id, index, binding_point;

    public UniformBuffer(String name, Shader shader, int size, int binding_point) {
        this.binding_point = binding_point;
        id = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, id);
        glBufferData(GL_UNIFORM_BUFFER, size, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER,0);

        index = glGetUniformBlockIndex(shader.getId(), name);
        glUniformBlockBinding(shader.getId(), index, binding_point);
        glBindBufferBase(GL_UNIFORM_BUFFER, binding_point, id);
    }

    public void update(ByteBuffer buffer) {
        glBindBuffer(GL_UNIFORM_BUFFER, id);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);
        glBindBuffer(GL_UNIFORM_BUFFER,0);
    }
}

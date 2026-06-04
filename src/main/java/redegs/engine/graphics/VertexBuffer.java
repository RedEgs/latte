package redegs.engine.graphics;

import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import java.util.List;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class VertexBuffer {
    private int id;

    public VertexBuffer() {
        id = glGenBuffers();
        // DON'T bind in constructor!
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void setLayout(int location, int size, int glEnumType, boolean normalized, int stride, int pointer) {
        glVertexAttribPointer(location, size, glEnumType, normalized, stride, pointer);
        glEnableVertexAttribArray(location);
    }

    // FIXED: stride should be in BYTES, not floats!
    public void setLayout(int location, int size, int glEnumType, int strideInFloats) {
        // Convert stride from floats to bytes
        int strideInBytes = strideInFloats * 4;
        setLayout(location, size, glEnumType, false, strideInBytes, 0);
    }

    public void setBufferData(FloatBuffer vertices) {
        bind();
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    public void setBufferData(List<Vertex> vertices) {
        FloatBuffer buffer = Vertex.toFloatBufferInterleaved(vertices);
        if (!buffer.isDirect()) {
            FloatBuffer directBuffer = BufferUtils.createFloatBuffer(buffer.capacity());
            directBuffer.put(buffer);
            directBuffer.rewind();
            buffer = directBuffer;
        }
        bind();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }
}
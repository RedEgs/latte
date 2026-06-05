package redegs.engine.graphics.buffers;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15C.*;

public class ElementBuffer {
    private int id;

    public ElementBuffer() {
        id = glGenBuffers();
    }

    public void setBufferData(IntBuffer indices) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }


}

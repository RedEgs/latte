package redegs.engine.graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.List;

public class Vertex {
    public FloatBuffer Position;
    public FloatBuffer Normal;
    public FloatBuffer UV;

    public static Vertex createVertex(float px, float py, float pz,
                                       float nx, float ny, float nz,
                                       float u, float v) {
        Vertex vertex = new Vertex();

        vertex.Position = BufferUtils.createFloatBuffer(3);
        vertex.Position.put(px).put(py).put(pz);
        vertex.Position.rewind();

        vertex.Normal = BufferUtils.createFloatBuffer(3);
        vertex.Normal.put(nx).put(ny).put(nz);
        vertex.Normal.rewind();

        vertex.UV = BufferUtils.createFloatBuffer(2);
        vertex.UV.put(u).put(v);
        vertex.UV.rewind();

        return vertex;
    }

    public static Vertex createVertex(Vector3f pos, Vector3f normal, Vector2f uv) {
        Vertex vertex = new Vertex();

        vertex.Position = BufferUtils.createFloatBuffer(3);
        vertex.Position.put(pos.x).put(pos.y).put(pos.z);
        vertex.Position.rewind();

        vertex.Normal = BufferUtils.createFloatBuffer(3);
        vertex.Normal.put(normal.x).put(normal.y).put(normal.z);
        vertex.Normal.rewind();

        vertex.UV = BufferUtils.createFloatBuffer(2);
        vertex.UV.put(uv.x).put(uv.y);  // correct
        vertex.UV.rewind();

        return vertex;
    }

    public FloatBuffer toFloatBufferInterleaved() {
        FloatBuffer interleaved = FloatBuffer.allocate(7); // 3+3+2 = 7 floats

        // Copy position (3 floats)
        interleaved.put(Position.duplicate());
        // Copy normal (3 floats)
        interleaved.put(Normal.duplicate());
        // Copy UV (2 floats)
        interleaved.put(UV.duplicate());

        interleaved.rewind(); // Reset position for reading
        return interleaved;
    }

    public static FloatBuffer toFloatBufferInterleaved(List<Vertex> vertices) {
        int stride = 8;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * stride);

        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);

            // Debug: print what we're about to put

            // Get position data
            v.Position.rewind();
            float px = v.Position.get();
            float py = v.Position.get();
            float pz = v.Position.get();

            // Get normal data
            v.Normal.rewind();
            float nx = v.Normal.get();
            float ny = v.Normal.get();
            float nz = v.Normal.get();

            // Get UV data
            v.UV.rewind();
            float u = v.UV.get();
            float vc = v.UV.get();  // rename to vc to avoid conflict with parameter


            // Reset and put all data
            v.Position.rewind();
            v.Normal.rewind();
            v.UV.rewind();

            buffer.put(v.Position);
            buffer.put(v.Normal);
            buffer.put(v.UV);
        }

        buffer.rewind();

        // Print final buffer

        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.print(buffer.get(i) + " ");
            if ((i + 1) % 8 == 0) System.out.println();
        }

        buffer.rewind();
        return buffer;
    }
}

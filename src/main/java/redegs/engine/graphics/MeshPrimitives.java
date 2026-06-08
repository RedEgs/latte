package redegs.engine.graphics;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static redegs.engine.graphics.Vertex.createVertex;

public class MeshPrimitives {
    public static Mesh skyboxCube() {
        List<Vertex> vertices = new ArrayList<>();

        // Front
        vertices.add(createVertex(-0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f,  0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f,  0.5f,  0.5f, 0, 0, 0, 0, 0));

        // Back
        vertices.add(createVertex( 0.5f, -0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f,  0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f,  0.5f, -0.5f, 0, 0, 0, 0, 0));

        // Top
        vertices.add(createVertex(-0.5f,  0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f,  0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f,  0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f,  0.5f, -0.5f, 0, 0, 0, 0, 0));

        // Bottom
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f, -0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 0));

        // Right
        vertices.add(createVertex( 0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f, -0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f,  0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex( 0.5f,  0.5f,  0.5f, 0, 0, 0, 0, 0));

        // Left
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f, -0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f,  0.5f,  0.5f, 0, 0, 0, 0, 0));
        vertices.add(createVertex(-0.5f,  0.5f, -0.5f, 0, 0, 0, 0, 0));

        // Reversed winding order (inside-facing)
        int[] indicesArray = {
                0, 3, 2, 2, 1, 0,
                4, 7, 6, 6, 5, 4,
                8,11,10,10, 9, 8,
                12,15,14,14,13,12,
                16,19,18,18,17,16,
                20,23,22,22,21,20
        };

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesArray.length);
        indicesBuffer.put(indicesArray).flip();

        return new Mesh(vertices, indicesBuffer, new ArrayList<>());
    }

    public static Mesh cube() {
        List<Vertex> vertices = new ArrayList<>();

        // Front face (z = 0.5f)
        vertices.add(createVertex(-0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        vertices.add(createVertex(0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f));
        vertices.add(createVertex(0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f));
        vertices.add(createVertex(-0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f));

        // Back face (z = -0.5f)
        vertices.add(createVertex(0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f));
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f));
        vertices.add(createVertex(-0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f));
        vertices.add(createVertex(0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f));

        // Top face (y = 0.5f)
        vertices.add(createVertex(-0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        vertices.add(createVertex(0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f));
        vertices.add(createVertex(0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f));
        vertices.add(createVertex(-0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f));

        // Bottom face (y = -0.5f)
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f));
        vertices.add(createVertex(0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f));
        vertices.add(createVertex(0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f));
        vertices.add(createVertex(-0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f));

        // Right face (x = 0.5f)
        vertices.add(createVertex(0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        vertices.add(createVertex(0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f));
        vertices.add(createVertex(0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f));
        vertices.add(createVertex(0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f));

        // Left face (x = -0.5f)
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        vertices.add(createVertex(-0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f));
        vertices.add(createVertex(-0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f));
        vertices.add(createVertex(-0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f));

        // CORRECTED INDICES - each face has 2 triangles in clockwise order
        int[] indicesArray = {
                // Front face (vertices 0-3)
                0, 1, 2, 2, 3, 0,
                // Back face (vertices 4-7)
                4, 5, 6, 6, 7, 4,
                // Top face (vertices 8-11)
                8, 9, 10, 10, 11, 8,
                // Bottom face (vertices 12-15)
                12, 13, 14, 14, 15, 12,
                // Right face (vertices 16-19)
                16, 17, 18, 18, 19, 16,
                // Left face (vertices 20-23)
                20, 21, 22, 22, 23, 20
        };

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(36);
        indicesBuffer.put(indicesArray);
        indicesBuffer.rewind();

        return new Mesh(vertices, indicesBuffer, new ArrayList<>());
    }

    public static Mesh quad() {
        List<Vertex> vertices = new ArrayList<>();

        // Just one square face (front)
        vertices.add(createVertex(-0.5f, -0.5f, 0.5f, 0, 0, 1, 0, 0));
        vertices.add(createVertex(0.5f, -0.5f, 0.5f, 0, 0, 1, 1, 0));
        vertices.add(createVertex(0.5f, 0.5f, 0.5f, 0, 0, 1, 1, 1));
        vertices.add(createVertex(-0.5f, 0.5f, 0.5f, 0, 0, 1, 0, 1));

        // Two triangles forming a square
        int[] indicesArray = {
                0, 1, 2,  // first triangle
                2, 3, 0   // second triangle
        };

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(6);
        indicesBuffer.put(indicesArray);
        indicesBuffer.rewind();

        return new Mesh(vertices, indicesBuffer, new ArrayList<>());
    }

    public static Mesh quadScreen() {
        List<Vertex> vertices = new ArrayList<>();

        // Vertices for a screen-filling quad (NDC coordinates: -1 to 1)
        // Order: bottom-left, bottom-right, top-right, top-left
        vertices.add(createVertex(-1f, -1f, 0f, 0f, 0f, 1f, 0f, 0f));  // Bottom-left
        vertices.add(createVertex(1f, -1f, 0f, 0f, 0f, 1f, 1f, 0f));  // Bottom-right
        vertices.add(createVertex(1f, 1f, 0f, 0f, 0f, 1f, 1f, 1f));  // Top-right
        vertices.add(createVertex(-1f, 1f, 0f, 0f, 0f, 1f, 0f, 1f));  // Top-left

        // Two triangles forming a fullscreen quad
        int[] indicesArray = {
                0, 1, 2,  // first triangle (bottom-left, bottom-right, top-right)
                2, 3, 0   // second triangle (top-right, top-left, bottom-left)
        };

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesArray.length);
        indicesBuffer.put(indicesArray);
        indicesBuffer.flip();  // Use flip() instead of rewind() for clarity

        return new Mesh(vertices, indicesBuffer, new ArrayList<>());
    }
}

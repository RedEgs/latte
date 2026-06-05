package redegs.engine.graphics;

import org.lwjgl.BufferUtils;
import redegs.engine.graphics.buffers.ElementBuffer;
import redegs.engine.graphics.buffers.VertexBuffer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.*;
import static redegs.engine.graphics.Vertex.createVertex;

public class Mesh {
    protected List<Vertex> vertices;
    protected IntBuffer indices;
    protected ArrayList<Material> materials;

    protected VertexArray vao;
    protected VertexBuffer vbo;
    protected ElementBuffer ebo;

    public Mesh(List<Vertex> vertices, IntBuffer indices, ArrayList<Material> materials) {
        this.vertices = vertices;
        this.indices = indices;
        this.materials = materials;

        createMesh();
    }

    protected void createMesh() {
        vao = new VertexArray();
        vao.bind();

        vbo = new VertexBuffer();
        vbo.bind();
        vbo.setBufferData(vertices);

        ebo = new ElementBuffer();
        ebo.bind();
        if (!indices.isDirect()) {
            IntBuffer directIndices = BufferUtils.createIntBuffer(indices.capacity());
            directIndices.put(indices);
            directIndices.rewind();
            indices = directIndices;
        }
        ebo.setBufferData(indices);

        // STRIDE IS 8 FLOATS = 32 BYTES
        vbo.setLayout(0, 3, GL_FLOAT, false, 32, 0);
        vbo.setLayout(1, 3, GL_FLOAT, false, 32, 12);
        vbo.setLayout(2, 2, GL_FLOAT, false, 32, 24);
        vao.unbind();
    }

    public void Draw(Shader shader) {
        for (Material material : materials) {
            material.apply(shader);
        }

        vao.bind();
        glDrawElements(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0);
        vao.unbind();
    }

    public List<Vertex> getVertices() {
        return vertices.stream().collect(Collectors.toList());
    }

    public IntBuffer getIndices() {
        return indices.asReadOnlyBuffer();
    }

    public List<Material> getMaterials() {
        return materials.stream().collect(Collectors.toList());
    }

    public void AddVertex(Vertex vertex, int pos) {
        vertices.add(pos, vertex);
    }

    public void AddMaterial(Material material, int pos) {
        materials.add(pos, material);
    }

    public void AddIndice(int indice, int pos) {
        indices.put(indice, pos);
    }



    public void AddVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public void AddMaterial(Material material) {
        materials.add(material);
    }

    public void AddIndice(int indice) {
        indices.put(indice);
    }

    public static Mesh cube() {
        List<Vertex> vertices = new ArrayList<>();

        // Front face (z = 0.5f)
        vertices.add(createVertex(-0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f));
        vertices.add(createVertex( 0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 0.0f));
        vertices.add(createVertex( 0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f));
        vertices.add(createVertex(-0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 1.0f));

        // Back face (z = -0.5f)
        vertices.add(createVertex( 0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f));
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f));
        vertices.add(createVertex(-0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f));
        vertices.add(createVertex( 0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f));

        // Top face (y = 0.5f)
        vertices.add(createVertex(-0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f));
        vertices.add(createVertex( 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f));
        vertices.add(createVertex( 0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f));
        vertices.add(createVertex(-0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f));

        // Bottom face (y = -0.5f)
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f));
        vertices.add(createVertex( 0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f));
        vertices.add(createVertex( 0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f));
        vertices.add(createVertex(-0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f));

        // Right face (x = 0.5f)
        vertices.add(createVertex( 0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f));
        vertices.add(createVertex( 0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f));
        vertices.add(createVertex( 0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f));
        vertices.add(createVertex( 0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f));

        // Left face (x = -0.5f)
        vertices.add(createVertex(-0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f));
        vertices.add(createVertex(-0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f));
        vertices.add(createVertex(-0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f));
        vertices.add(createVertex(-0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f));

        // CORRECTED INDICES - each face has 2 triangles in clockwise order
        int[] indicesArray = {
                // Front face (vertices 0-3)
                0, 1, 2,  2, 3, 0,
                // Back face (vertices 4-7)
                4, 5, 6,  6, 7, 4,
                // Top face (vertices 8-11)
                8, 9, 10,  10, 11, 8,
                // Bottom face (vertices 12-15)
                12, 13, 14,  14, 15, 12,
                // Right face (vertices 16-19)
                16, 17, 18,  18, 19, 16,
                // Left face (vertices 20-23)
                20, 21, 22,  22, 23, 20
        };

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(36);
        indicesBuffer.put(indicesArray);
        indicesBuffer.rewind();

        return new Mesh(vertices, indicesBuffer, new ArrayList<>());
    }

    public static Mesh quad() {
        List<Vertex> vertices = new ArrayList<>();

        // Just one square face (front)
        vertices.add(createVertex(-0.5f, -0.5f, 0.5f, 0,0,1, 0,0));
        vertices.add(createVertex( 0.5f, -0.5f, 0.5f, 0,0,1, 1,0));
        vertices.add(createVertex( 0.5f,  0.5f, 0.5f, 0,0,1, 1,1));
        vertices.add(createVertex(-0.5f,  0.5f, 0.5f, 0,0,1, 0,1));

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
        vertices.add(createVertex( 1f, -1f, 0f, 0f, 0f, 1f, 1f, 0f));  // Bottom-right
        vertices.add(createVertex( 1f,  1f, 0f, 0f, 0f, 1f, 1f, 1f));  // Top-right
        vertices.add(createVertex(-1f,  1f, 0f, 0f, 0f, 1f, 0f, 1f));  // Top-left

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

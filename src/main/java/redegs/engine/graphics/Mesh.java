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


    private int findVertexIndex(List<Vertex> vertices, Vertex target) {
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            if (v.Position.equals(target.Position) &&
                    v.Normal.equals(target.Normal) &&
                    v.UV.equals(target.UV)) {
                return i;
            }
        }
        return -1;
    }


    public void Draw(Shader shader) {
        for (Material material : materials) {
            material.apply(shader);
        }

        vao.bind();
        glDrawElements(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0);
        vao.unbind();
    }


    public void DrawNoMaterials(Shader shader) {
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

}

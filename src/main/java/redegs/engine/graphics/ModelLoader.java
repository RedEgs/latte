package redegs.engine.graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

    private static final int ASSIMP_FLAGS =
            aiProcess_Triangulate       |
                    aiProcess_GenSmoothNormals  |
                    aiProcess_CalcTangentSpace  |
                    aiProcess_LimitBoneWeights  |
                    aiProcess_ImproveCacheLocality |
                    aiProcess_SortByPType       |
                    aiProcess_FindDegenerates   |
                    aiProcess_FindInvalidData   |
                    aiProcess_FlipUVs;

    public static List<Mesh> load(String path, int entity) {
        return load(path, ASSIMP_FLAGS, entity);
    }

    public static List<Mesh> load(String path, int flags, int entity) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("Model file not found: " + path);
        }

        String directory = file.getParent();
        if (directory == null) directory = ".";

        AIScene scene = aiImportFile(path, flags);
        if (scene == null) {
            throw new RuntimeException("Failed to load model: " + aiGetErrorString());
        }

        List<Mesh> meshes = new ArrayList<>();
        processNode(scene.mRootNode(), scene, meshes, directory, entity);

        aiReleaseImport(scene);
        return meshes;
    }

    // ---- Node traversal ----

    private static void processNode(AINode node, AIScene scene, List<Mesh> meshes, String directory, int entity) {
        if (node == null) return;

        IntBuffer nodeMeshes = node.mMeshes();
        for (int i = 0; i < node.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(nodeMeshes.get(i)));
            meshes.add(processMesh(aiMesh, scene, directory, entity));
        }

        PointerBuffer children = node.mChildren();
        for (int i = 0; i < node.mNumChildren(); i++) {
            processNode(AINode.create(children.get(i)), scene, meshes, directory, entity);
        }
    }

    // ---- Mesh processing ----

    private static Mesh processMesh(AIMesh aiMesh, AIScene scene, String directory, int entity) {
        List<Vertex> vertices = extractVertices(aiMesh);
        IntBuffer indices = extractIndices(aiMesh);
        Material material = extractMaterial(aiMesh, scene, directory, entity);

        ArrayList<Material> materials = new ArrayList<>();
        materials.add(material);

        return new Mesh(vertices, indices, materials);
    }

    private static List<Vertex> extractVertices(AIMesh aiMesh) {
        List<Vertex> vertices = new ArrayList<>();

        AIVector3D.Buffer aiPositions = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals   = aiMesh.mNormals();
        AIVector3D.Buffer aiTexCoords = aiMesh.mTextureCoords(0);

        for (int i = 0; i < aiMesh.mNumVertices(); i++) {
            AIVector3D v = aiPositions.get(i);
            Vector3f position = new Vector3f(v.x(), v.y(), v.z());

            Vector3f normal = new Vector3f(0, 0, 0);
            if (aiNormals != null) {
                AIVector3D n = aiNormals.get(i);
                normal.set(n.x(), n.y(), n.z());
            }

            Vector2f uv = new Vector2f(0, 0);
            if (aiTexCoords != null) {
                AIVector3D t = aiTexCoords.get(i);
                uv.set(t.x(), t.y());
            }

            vertices.add(Vertex.createVertex(position, normal, uv));
        }

        return vertices;
    }

    private static IntBuffer extractIndices(AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < aiMesh.mNumFaces(); i++) {
            AIFace face = aiMesh.mFaces().get(i);
            IntBuffer faceIndices = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); j++) {
                indices.add(faceIndices.get(j));
            }
        }

        IntBuffer buffer = BufferUtils.createIntBuffer(indices.size());
        for (int idx : indices) buffer.put(idx);
        buffer.flip();
        return buffer;
    }

    // ---- Material processing ----

    private static Material extractMaterial(AIMesh aiMesh, AIScene scene, String directory, int entity) {
        if (aiMesh.mMaterialIndex() < 0) {
            return defaultMaterial();
        }

        AIMaterial aiMaterial = AIMaterial.create(
                scene.mMaterials().get(aiMesh.mMaterialIndex())
        );

        try (MemoryStack stack = MemoryStack.stackPush()) {
            AIColor4D color = AIColor4D.create();

            Vector3f ambient = new Vector3f(0.2f, 0.2f, 0.2f);
            if (aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                ambient.set(color.r(), color.g(), color.b());
            }

            float shininess = 32.0f;
            float[] shinArr = new float[1];
            if (aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS, aiTextureType_NONE, 0, shinArr, null) == aiReturn_SUCCESS) {
                shininess = shinArr[0];
            }

            Texture diffuse  = loadTexture(aiMaterial, aiTextureType_DIFFUSE,  directory, stack, entity);
            Texture specular = loadTexture(aiMaterial, aiTextureType_SPECULAR, directory, stack, entity);

            return new Material(ambient, diffuse, specular, shininess);
        }
    }

    private static Texture loadTexture(AIMaterial mat, int type, String directory, MemoryStack stack, int entity) {
        AIString path = AIString.calloc(stack);

        int result = aiGetMaterialTexture(mat, type, 0, path,
                (IntBuffer) null, null, null, null, null, null);

        if (result != aiReturn_SUCCESS) return null;

        String texPath = path.dataString();

        File file = new File(directory, texPath);
        if (file.exists()) return new Texture(file.getAbsolutePath(), entity);

        file = new File(texPath);
        if (file.exists()) return new Texture(texPath, entity);

        return null;
    }

    private static Material defaultMaterial() {
        return new Material(new Vector3f(0.8f, 0.8f, 0.8f), null, null, 32.0f);
    }
}
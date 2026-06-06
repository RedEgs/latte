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
            aiProcess_Triangulate |
                    aiProcess_GenSmoothNormals |
                    aiProcess_CalcTangentSpace |
                    aiProcess_LimitBoneWeights |
                    aiProcess_ImproveCacheLocality |
                    aiProcess_SortByPType |
                    aiProcess_FindDegenerates |
                    aiProcess_FindInvalidData |
                    aiProcess_FlipUVs;

    public static Model loadModel(String path) {
        return loadModel(path, ASSIMP_FLAGS);
    }

    public static Model loadModel(String path, int flags) {
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

        Model model = new Model();
        processNode(scene.mRootNode(), scene, model, directory);

        aiReleaseImport(scene);
        return model;
    }

    private static void processNode(AINode node, AIScene scene, Model model, String directory) {
        if (node == null) return;

        IntBuffer meshes = node.mMeshes();
        int meshCount = node.mNumMeshes();

        for (int i = 0; i < meshCount; i++) {
            int meshIndex = meshes.get(i);
            AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(meshIndex));
            model.addMesh(processMesh(aiMesh, scene, directory));
        }

        PointerBuffer children = node.mChildren();
        int childCount = node.mNumChildren();

        for (int i = 0; i < childCount; i++) {
            AINode child = AINode.create(children.get(i));
            processNode(child, scene, model, directory);
        }
    }

    private static Mesh processMesh(AIMesh aiMesh, AIScene scene, String directory) {

        List<Vertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // --- vertices ---
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        AIVector3D.Buffer aiTexCoords = aiMesh.mTextureCoords(0);

        for (int i = 0; i < aiMesh.mNumVertices(); i++) {

            AIVector3D v = aiVertices.get(i);
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

        // --- indices ---
        for (int i = 0; i < aiMesh.mNumFaces(); i++) {
            AIFace face = aiMesh.mFaces().get(i);
            IntBuffer faceIdx = face.mIndices();

            for (int j = 0; j < face.mNumIndices(); j++) {
                indices.add(faceIdx.get(j));
            }
        }

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
        for (int idx : indices) indexBuffer.put(idx);
        indexBuffer.flip();

        // --- material ---
        Material material = new Material(
                new Vector3f(0.8f, 0.8f, 0.8f),
                null,
                null,
                32.0f
        );

        if (aiMesh.mMaterialIndex() >= 0) {
            AIMaterial aiMaterial = AIMaterial.create(
                    scene.mMaterials().get(aiMesh.mMaterialIndex())
            );
            material = processMaterial(aiMaterial, directory);
        }

        ArrayList<Material> materials = new ArrayList<>();
        materials.add(material);

        return new Mesh(vertices, indexBuffer, materials);
    }

    private static Material processMaterial(AIMaterial mat, String directory) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            AIColor4D color = AIColor4D.create();

            Vector3f ambient = new Vector3f(0.2f, 0.2f, 0.2f);
            if (aiGetMaterialColor(mat, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color)
                    == aiReturn_SUCCESS) {
                ambient.set(color.r(), color.g(), color.b());
            }

            Texture diffuse = loadTexture(mat, aiTextureType_DIFFUSE, directory, stack);
            Texture specular = loadTexture(mat, aiTextureType_SPECULAR, directory, stack);

            float shininess = 32.0f;
            float[] shinArr = new float[1];

            if (aiGetMaterialFloatArray(mat, AI_MATKEY_SHININESS, aiTextureType_NONE, 0, shinArr, null)
                    == aiReturn_SUCCESS) {
                shininess = shinArr[0];
            }

            return new Material(ambient, diffuse, specular, shininess);
        }
    }

    private static Texture loadTexture(AIMaterial mat, int type, String directory, MemoryStack stack) {

        AIString path = AIString.calloc(stack);

        int result = aiGetMaterialTexture(
                mat,
                type,
                0,
                path,
                (IntBuffer) null,
                null,
                null,
                null,
                null,
                null
        );

        if (result != aiReturn_SUCCESS) return null;

        String texPath = path.dataString();

        File file = new File(directory, texPath);
        if (file.exists()) return new Texture(file.getAbsolutePath());

        file = new File(texPath);
        if (file.exists()) return new Texture(texPath);

        return null;
    }

    // ---------------- DATA LOADER ----------------

    public static ModelData loadModelData(String path) {

        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("Model file not found: " + path);
        }

        AIScene scene = aiImportFile(path, ASSIMP_FLAGS);
        if (scene == null) {
            throw new RuntimeException(aiGetErrorString());
        }

        ModelData data = new ModelData();
        String dir = file.getParent();
        if (dir == null) dir = ".";

        processNodeForData(scene.mRootNode(), scene, data, dir);

        aiReleaseImport(scene);
        return data;
    }

    private static void processNodeForData(AINode node, AIScene scene, ModelData data, String dir) {

        IntBuffer meshes = node.mMeshes();
        for (int i = 0; i < node.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshes.get(i)));
            data.addMeshData(extractMeshData(mesh));
        }

        PointerBuffer children = node.mChildren();
        for (int i = 0; i < node.mNumChildren(); i++) {
            AINode child = AINode.create(children.get(i));
            processNodeForData(child, scene, data, dir);
        }
    }

    private static MeshData extractMeshData(AIMesh mesh) {

        MeshData data = new MeshData();

        int vCount = mesh.mNumVertices();

        data.vertices = new float[vCount * 3];
        data.normals = new float[vCount * 3];
        data.texCoords = new float[vCount * 2];

        AIVector3D.Buffer v = mesh.mVertices();
        AIVector3D.Buffer n = mesh.mNormals();
        AIVector3D.Buffer t = mesh.mTextureCoords(0);

        for (int i = 0; i < vCount; i++) {

            AIVector3D vv = v.get(i);
            data.vertices[i * 3] = vv.x();
            data.vertices[i * 3 + 1] = vv.y();
            data.vertices[i * 3 + 2] = vv.z();

            if (n != null) {
                AIVector3D nn = n.get(i);
                data.normals[i * 3] = nn.x();
                data.normals[i * 3 + 1] = nn.y();
                data.normals[i * 3 + 2] = nn.z();
            }

            if (t != null) {
                AIVector3D tt = t.get(i);
                data.texCoords[i * 2] = tt.x();
                data.texCoords[i * 2 + 1] = tt.y();
            }
        }

        List<Integer> idx = new ArrayList<>();

        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace f = mesh.mFaces().get(i);
            IntBuffer ib = f.mIndices();
            for (int j = 0; j < f.mNumIndices(); j++) {
                idx.add(ib.get(j));
            }
        }

        data.indices = idx.stream().mapToInt(i -> i).toArray();

        return data;
    }

    // ---------------- DATA TYPES ----------------

    public static class ModelData {
        private final List<MeshData> meshes = new ArrayList<>();

        public void addMeshData(MeshData mesh) {
            meshes.add(mesh);
        }

        public List<MeshData> getMeshes() {
            return meshes;
        }
    }

    public static class MeshData {
        public float[] vertices;
        public int[] indices;
        public float[] normals;
        public float[] texCoords;
        public String diffuseTexture;
        public String specularTexture;
        public float shininess;
        public Vector3f ambient;
    }
}
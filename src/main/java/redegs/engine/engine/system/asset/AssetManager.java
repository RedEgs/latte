package redegs.engine.engine.system.asset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import redegs.engine.graphics.Material;
import redegs.engine.graphics.Texture;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    private static final Map<Class<?>, Map<String, Object>> assets = new HashMap<>();

    public static <T> void register(String path, T asset) {
        assets.computeIfAbsent(asset.getClass(), k -> new HashMap<>())
                .put(path, asset);
    }

    public static void clear() {
        assets.clear();
    }

    public static <T> T get(Class<T> type, String path) {
        Map<String, Object> map = assets.get(type);

        if (map == null)
            return null;

        return type.cast(map.get(path));
    }

    public static <T> Collection<T> getAll(Class<T> type) {
        Map<String, Object> map = assets.get(type);

        if (map == null)
            return Collections.emptyList();

        return map.values()
                .stream()
                .map(type::cast)
                .toList();
    }

    public static JsonObject Save() {
        JsonObject root = new JsonObject();

        JsonArray materials = new JsonArray();
        Map<String, Object> materialMap = assets.get(Material.class);
        if (materialMap != null) {
            for (Map.Entry<String, Object> entry : materialMap.entrySet()) {
                JsonObject materialJson = ((Material) entry.getValue()).Save();
                materialJson.addProperty("assetKey", entry.getKey());
                materials.add(materialJson);
            }
        }
        root.add("materials", materials);

        JsonArray textures = new JsonArray();
        Map<String, Object> textureMap = assets.get(Texture.class);
        if (textureMap != null) {
            for (Map.Entry<String, Object> entry : textureMap.entrySet()) {
                JsonObject textureJson = ((Texture) entry.getValue()).Save();
                textureJson.addProperty("assetKey", entry.getKey());
                textures.add(textureJson);
            }
        }
        root.add("textures", textures);

        JsonArray models = new JsonArray();
        Map<String, Object> modelMap = assets.get(ModelAsset.class);
        if (modelMap != null) {
            for (Map.Entry<String, Object> entry : modelMap.entrySet()) {
                ModelAsset asset = (ModelAsset) entry.getValue();
                JsonObject modelJson = new JsonObject();
                modelJson.addProperty("assetKey", entry.getKey());
                modelJson.addProperty("path", asset.getPath());
                models.add(modelJson);
            }
        }
        root.add("models", models);

        return root;
    }

    public static void Load(JsonObject root) {
        if (root == null) {
            return;
        }

        if (root.has("textures") && root.get("textures").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("textures")) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject textureJson = element.getAsJsonObject();
                Texture texture = Texture.fromJson(textureJson, -1);
                register(assetKey(textureJson, texture.getAssetKey()), texture);
            }
        }

        if (root.has("materials") && root.get("materials").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("materials")) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject materialJson = element.getAsJsonObject();
                String key = assetKey(materialJson, "Material");
                Material material = Material.fromJson(materialJson, -1, key);
                register(key, material);
            }
        }

        if (root.has("models") && root.get("models").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("models")) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject modelJson = element.getAsJsonObject();
                if (!modelJson.has("path") || modelJson.get("path").isJsonNull()) {
                    continue;
                }

                ModelAsset asset = new ModelAsset(modelJson.get("path").getAsString());
                register(assetKey(modelJson, asset.getPath()), asset);
            }
        }
    }

    private static String assetKey(JsonObject json, String fallback) {
        if (json.has("assetKey") && !json.get("assetKey").isJsonNull()) {
            return json.get("assetKey").getAsString();
        }

        return fallback;
    }
}

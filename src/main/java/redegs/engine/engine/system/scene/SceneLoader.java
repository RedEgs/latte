package redegs.engine.engine.system.scene;

import com.google.gson.*;
import org.joml.Vector3f;
import redegs.engine.engine.adapters.*;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.asset.AssetManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.graphics.Model;
import redegs.engine.graphics.Transform;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneLoader {
    private final Map<String, Class<? extends Component>> registry = new HashMap<>();

    public void register(Class<? extends Component> type) {
        registry.put(type.getSimpleName(), type);
    }

    public Scene LoadScene(String path) throws FileNotFoundException {
        JsonObject root = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
        Scene scene = new Scene();

        AssetManager.clear();
        if (root.has("assets") && root.get("assets").isJsonObject()) {
            AssetManager.Load(root.getAsJsonObject("assets"));
        }

        for (JsonElement el : root.getAsJsonArray("entities")) {
            JsonObject eo = el.getAsJsonObject();
            int entity = scene.createEntity();

            JsonObject comps = eo.getAsJsonObject("components");

            // Sort so Transform is always loaded before anything that depends on it (e.g. Model)
            List<Map.Entry<String, JsonElement>> componentList = new ArrayList<>(comps.entrySet());
            componentList.sort((a, b) -> {
                if (a.getKey().equals("Transform")) return -1;
                if (b.getKey().equals("Transform")) return 1;
                return 0;
            });

            for (Map.Entry<String, JsonElement> entry : componentList) {
                Class<? extends Component> clazz = registry.get(entry.getKey());
                if (clazz == null) {
                    throw new RuntimeException("No component registered for type: " + entry.getKey());
                }

                Component c = instantiate(clazz, entity);
                c.Load(comps.getAsJsonObject(entry.getKey()));
                scene.addComponent(entity, c);
            }
        }

        return scene;
    }

    private Component instantiate(Class<? extends Component> clazz, int entity) {
        try {
            // requires a (int entity) constructor on every Component — see note below
            return clazz.getDeclaredConstructor(int.class).newInstance(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName(), e);
        }
    }

    public void SaveScene(Scene scene, String path) throws IOException {
        JsonArray entitiesJson = new JsonArray();

        for (int entity = 0; entity < scene.getEntities(); entity++) {
            JsonObject eo = new JsonObject();
            eo.addProperty("id", entity);

            JsonObject compsJson = new JsonObject();
            for (Object o : scene.getComponents(entity)) {
                if (o instanceof Component c) {
                    compsJson.add(c.getClass().getSimpleName(), c.Save());
                }
            }
            eo.add("components", compsJson);
            entitiesJson.add(eo);
        }

        JsonObject root = new JsonObject();
        root.add("assets", AssetManager.Save());
        root.add("entities", entitiesJson);

        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // only used for the outer shell now
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(root, writer);
        }
    }
}

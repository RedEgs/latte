package redegs.engine.engine.system.asset;

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
}
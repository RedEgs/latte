package redegs.engine.engine.system.asset;

import java.nio.file.Path;

public final class ModelAsset {
    private final String name;
    private final String path;

    public ModelAsset(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Model asset path cannot be empty.");
        }

        this.path = path;

        Path filename = Path.of(path).getFileName();
        this.name = filename != null ? filename.toString() : path;
    }

    public static ModelAsset importFile(String path) {
        ModelAsset asset = new ModelAsset(path);
        AssetManager.register(asset.getPath(), asset);
        return asset;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}

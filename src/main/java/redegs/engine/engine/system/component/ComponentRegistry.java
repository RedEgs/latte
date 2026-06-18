package redegs.engine.engine.system.component;

import java.util.*;
import java.util.function.Supplier;

public final class ComponentRegistry {

    public record Entry(
            String name,
            String category,
            String description,
            Class<? extends Component> type,
            Supplier<Component> factory   // creates an instance with a temp entity
    ) {}

    private static final Map<String, Entry> BY_NAME = new LinkedHashMap<>();
    private static final Map<String, List<Entry>> BY_CATEGORY = new LinkedHashMap<>();

    private ComponentRegistry() {}

    public static void register(Class<? extends Component> type, Supplier<Component> factory) {
        ComponentMeta meta = type.getAnnotation(ComponentMeta.class);
        if (meta == null) return;

        Entry entry = new Entry(meta.name(), meta.category(), meta.description(), type, factory);
        BY_NAME.put(meta.name(), entry);
        BY_CATEGORY.computeIfAbsent(meta.category(), k -> new ArrayList<>()).add(entry);
    }

    public static Optional<Entry> find(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    public static List<Entry> all() {
        return List.copyOf(BY_NAME.values());
    }

    public static List<Entry> byCategory(String category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    public static Set<String> categories() {
        return BY_CATEGORY.keySet();
    }
}
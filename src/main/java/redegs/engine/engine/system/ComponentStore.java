package redegs.engine.engine.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentStore<T> {
    private final Map<Integer, T> components = new HashMap<>();

    public void add(int entity, T component) {
        components.put(entity, component);
    }

    public T get(int entity) {
        return components.get(entity);
    }

    public void remove(int entity) {
        components.remove(entity);
    }

    public boolean has(int entity) {
        return components.containsKey(entity);
    }

    public Map<Integer, T> getComponents() {
        return components;
    }

    public List<T> toList() {
        return new ArrayList<>(components.values());
    }

}
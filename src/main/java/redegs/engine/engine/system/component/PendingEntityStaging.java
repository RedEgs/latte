package redegs.engine.engine.system.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PendingEntityStaging {
    private int nextEntity = 0;
    private Map<Class<?>, ComponentStore<?>> stores = new HashMap<>();

    public int createEntity() { return nextEntity++; }

    public <T> void addComponent(int entity, T component) {
        getStore((Class<T>) component.getClass()).add(entity, component);
    }

    @SuppressWarnings("unchecked")
    public <T> ComponentStore<T> getStore(Class<T> type) {
        return (ComponentStore<T>) stores.computeIfAbsent(type, k -> new ComponentStore<>());
    }

    public boolean isEmpty() { return stores.isEmpty(); }
    public int getNextEntity() { return nextEntity; }
    public HashMap<Class<?>, ComponentStore<?>> getStores() { return (HashMap<Class<?>, ComponentStore<?>>) stores; }

    public List<?> getComponents(int entity) {
        List<Object> objects = new ArrayList<>();
        for (ComponentStore<?> cs : getStores().values()) {
            Object c = cs.get(entity);
            if (c != null) {
                objects.add(c);
            }
        }
        return objects;
    }

    public void clear() {
        stores = new HashMap<>();
        nextEntity = 0;
    }
}
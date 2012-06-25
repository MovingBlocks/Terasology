package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A table for storing entities and components. Focused on allowing iteration across a components of a given type
 *
 * @author Immortius <immortius@gmail.com>
 */
class ComponentTable {
    private Map<Class, TIntObjectMap<Component>> store = new HashMap<Class, TIntObjectMap<Component>>();

    public <T extends Component> T get(int entityId, Class<T> componentClass) {
        TIntObjectMap<Component> entityMap = store.get(componentClass);
        if (entityMap != null) {
            return componentClass.cast(entityMap.get(entityId));
        }
        return null;
    }

    public Component put(int entityId, Component component) {
        TIntObjectMap<Component> entityMap = store.get(component.getClass());
        if (entityMap == null) {
            entityMap = new TIntObjectHashMap<Component>();
            store.put(component.getClass(), entityMap);
        }
        return entityMap.put(entityId, component);
    }

    public <T extends Component> Component remove(int entityId, Class<T> componentClass) {
        TIntObjectMap<Component> entityMap = store.get(componentClass);
        if (entityMap != null) {
            return entityMap.remove(entityId);
        }
        return null;
    }

    public void remove(int entityId) {
        for (TIntObjectMap<Component> entityMap : store.values()) {
            entityMap.remove(entityId);
        }
    }

    public void clear() {
        store.clear();
    }

    public int getComponentCount(Class<? extends Component> componentClass) {
        TIntObjectMap<Component> map = store.get(componentClass);
        if (map == null)
            return 0;
        return map.size();
    }

    public Iterable<Component> iterateComponents(int entityId) {
        List<Component> components = Lists.newArrayList();
        for (TIntObjectMap<Component> componentMap : store.values()) {
            Component comp = componentMap.get(entityId);
            if (comp != null) {
                components.add(comp);
            }
        }
        return components;
    }

    public <T extends Component> TIntObjectIterator<T> componentIterator(Class<T> componentClass) {
        TIntObjectMap<T> entityMap = (TIntObjectMap<T>) store.get(componentClass);
        if (entityMap != null) {
            return entityMap.iterator();
        }
        return null;
    }

    /**
     * Produces an iterator for iterating over all entities
     * <p/>
     * This is not designed to be performant, and in general usage entities should not be iterated over.
     *
     * @return An iterator over all entity ids.
     */
    public TIntIterator entityIdIterator() {
        TIntSet idSet = new TIntHashSet();
        for (TIntObjectMap<Component> componentMap : store.values()) {
            idSet.addAll(componentMap.keys());
        }
        return idSet.iterator();
    }

}

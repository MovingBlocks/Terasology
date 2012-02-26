package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.terasology.entitySystem.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A table for storing entities and components. Focused on allowing iteration across a components of a given type
 * @author Immortius <immortius@gmail.com>
 */
class ComponentTable {
    private Map<Class, TLongObjectMap<Component>> store = new HashMap<Class, TLongObjectMap<Component>>();
    
    public <T extends Component> T get(long entityId, Class<T> componentClass) {
        TLongObjectMap<Component> entityMap = store.get(componentClass);
        if (entityMap != null) {
            return componentClass.cast(entityMap.get(entityId));
        }
        return null;
    }

    public Component put(long entityId, Component component) {
        TLongObjectMap<Component> entityMap = store.get(component.getClass());
        if (entityMap == null) {
            entityMap = new TLongObjectHashMap<Component>();
            store.put(component.getClass(), entityMap);
        }
        return entityMap.put(entityId, component);
    }

    public <T extends Component> Component remove(long entityId, Class<T> componentClass) {
        TLongObjectMap<Component> entityMap = store.get(componentClass);
        if (entityMap != null) {
            return entityMap.remove(entityId);
        }
        return null;
    }

    public void remove(long entityId) {
        for (TLongObjectMap<Component> entityMap : store.values()) {
            entityMap.remove(entityId);
        }
    }
    
    public Iterable<Component> iterateComponents(long entityId) {
        List<Component> components = Lists.newArrayList();
        for (TLongObjectMap<Component> componentMap : store.values()) {
            Component comp = componentMap.get(entityId);
            if (comp != null) {
                components.add(comp);
            }
        }
        return components;
    }
    
    public <T extends Component> TLongObjectIterator<T> componentIterator(Class<T> componentClass) {
        TLongObjectMap<T> entityMap = (TLongObjectMap<T>) store.get(componentClass);
        if (entityMap != null) {
            return entityMap.iterator();
        }
        return null;
    } 
    
}

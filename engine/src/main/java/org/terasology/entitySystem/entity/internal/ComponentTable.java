/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.entitySystem.entity.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.terasology.entitySystem.Component;

import java.util.List;
import java.util.Map;

/**
 * A table for storing entities and components. Focused on allowing iteration across a components of a given type
 *
 */
class ComponentTable {
    private Map<Class<?>, TLongObjectMap<Component>> store = Maps.newConcurrentMap();

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
            entityMap = new TLongObjectHashMap<>();
            store.put(component.getClass(), entityMap);
        }
        return entityMap.put(entityId, component);
    }

    /**
     *
     * @return removes the component with the specified class from the entity and returns it.
     *         Returns null if no component could be removed.
     */
    public <T extends Component> Component remove(long entityId, Class<T> componentClass) {
        TLongObjectMap<Component> entityMap = store.get(componentClass);
        if (entityMap != null) {
            return entityMap.remove(entityId);
        }
        return null;
    }


    public List<Component> removeAndReturnComponentsOf(long entityId) {
        List<Component> componentList = Lists.newArrayList();
        for (TLongObjectMap<Component> entityMap : store.values()) {
            Component component = entityMap.remove(entityId);
            if (component != null) {
                componentList.add(component);
            }
        }
        return componentList;
    }

    public void remove(long entityId) {
        for (TLongObjectMap<Component> entityMap : store.values()) {
            entityMap.remove(entityId);
        }
    }

    public void clear() {
        store.clear();
    }

    public int getComponentCount(Class<? extends Component> componentClass) {
        TLongObjectMap<Component> map = store.get(componentClass);
        return (map == null) ? 0 : map.size();
    }

    /**
     *
     * @return an iterable that should be only used for iteration over the components. It can't be used to remove
     *         components. It should not be used after components have been added or removed from the entity.
     *
     */
    public Iterable<Component> iterateComponents(long entityId) {
        return getComponentsInNewList(entityId);
    }

    /**
     *
     * @return a new modifable list instance that contains all the components the entity had at the
     *         time this method got called.
     */
    public List<Component> getComponentsInNewList(long entityId) {
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

    /**
     * Produces an iterator for iterating over all entities
     * <br><br>
     * This is not designed to be performant, and in general usage entities should not be iterated over.
     *
     * @return An iterator over all entity ids.
     */
    public TLongIterator entityIdIterator() {
        TLongSet idSet = new TLongHashSet();
        for (TLongObjectMap<Component> componentMap : store.values()) {
            idSet.addAll(componentMap.keys());
        }
        return idSet.iterator();
    }

    public int numEntities() {
        TLongSet idSet = new TLongHashSet();
        for (TLongObjectMap<Component> componentMap : store.values()) {
            idSet.addAll(componentMap.keys());
        }
        return idSet.size();
    }

}

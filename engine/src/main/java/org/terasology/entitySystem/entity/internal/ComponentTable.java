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
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import org.terasology.entitySystem.Component;

import java.util.List;
import java.util.Map;

/**
 * A table for storing entities and components. Focused on allowing iteration across a components of a given type
 *
 * @author Immortius <immortius@gmail.com>
 */
class ComponentTable {
    private Map<Class, TLongObjectMap<Component>> store = Maps.newConcurrentMap();

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

    public void clear() {
        store.clear();
    }

    public int getComponentCount(Class<? extends Component> componentClass) {
        TLongObjectMap<Component> map = store.get(componentClass);
        return (map == null) ? 0 : map.size();
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

    /**
     * Produces an iterator for iterating over all entities
     * <p/>
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

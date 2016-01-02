/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.entitySystem.entity.lifecycleEvents;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated Use the prefab delta system instead (create a json file under /deltas/moduleName/prefabs/prefabName.prefab with the desired changes)
 */
@Deprecated
public class BeforeEntityCreated implements Event {
    private Prefab prefab;
    private Iterable<Component> components;
    private Set<Class<? extends Component>> componentsToRemove = Sets.newLinkedHashSet();
    private Map<Class<? extends Component>, Component> componentsToAdd = Maps.newLinkedHashMap();

    public BeforeEntityCreated(Prefab prefab, Iterable<Component> components) {
        this.prefab = prefab;
        this.components = components;
    }

    public Prefab getPrefab() {
        return prefab;
    }

    public Iterable<Component> getOriginalComponents() {
        return components;
    }

    public void addComponent(Component component) {
        if (componentsToAdd.containsKey(component.getClass())) {
            throw new IllegalArgumentException("Tried adding the same component multiple times");
        }
        componentsToAdd.put(component.getClass(), component);
    }

    public void prohibitComponent(Class<? extends Component> componentClass) {
        componentsToRemove.add(componentClass);
    }

    public Iterable<Component> getResultComponents() {
        return IteratorImpl::new;
    }

    private final class IteratorImpl implements Iterator<Component> {
        private Iterator<Component> sourceIterator;
        private Iterator<Component> addedIterator;

        private Component next;

        private IteratorImpl() {
            sourceIterator = components.iterator();
            addedIterator = componentsToAdd.values().iterator();
            next = getNext();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Component next() {
            Component result = next;
            next = getNext();
            return result;
        }

        private Component getNext() {
            while (sourceIterator.hasNext()) {
                final Component result = sourceIterator.next();
                if (componentsToAdd.containsKey(result.getClass())) {
                    throw new IllegalStateException("Requested to add component that was already defined for this entity");
                }
                if (componentsToRemove.contains(result.getClass())) {
                    continue;
                }
                return result;
            }
            while (addedIterator.hasNext()) {
                final Component result = addedIterator.next();
                if (componentsToRemove.contains(result.getClass())) {
                    continue;
                }
                return result;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for read-only iterator");
        }
    }
}

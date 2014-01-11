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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.event.Event;
import org.terasology.world.block.BlockUri;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class EntityBeingGenerated implements Event {
    private String prefabName;
    private BlockUri blockUri;
    private Iterable<Component> components;
    private Set<Class<?>> componentsToRemove = new HashSet<Class<?>>();
    private Map<Class, Component> componentsToAdd = new HashMap<Class, Component>();

    public EntityBeingGenerated(String prefabName, BlockUri blockUri, Iterable<Component> components) {
        this.prefabName = prefabName;
        this.blockUri = blockUri;
        this.components = components;
    }

    public BlockUri getBlockUri() {
        return blockUri;
    }

    public String getPrefabName() {
        return prefabName;
    }

    public Iterable<Component> getOriginalComponents() {
        return components;
    }

    public void addComponent(Component component) {
        if (componentsToAdd.containsKey(component.getClass()))
            throw new IllegalArgumentException("Tried adding the same component multiple times");
        componentsToAdd.put(component.getClass(), component);
    }

    public void prohibitComponent(Class<Component> componentClass) {
        componentsToRemove.add(componentClass);
    }

    public Iterable<Component> getResultComponents() {
        return new Iterable<Component>() {
            @Override
            public Iterator<Component> iterator() {
                return new IteratorImpl();
            }
        };
    }

    private class IteratorImpl implements Iterator<Component> {
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
                if (componentsToAdd.containsKey(result.getClass()))
                    throw new IllegalStateException("Requested to add component that was already defined for this entity");
                if (componentsToRemove.contains(result.getClass()))
                    continue;
                return result;
            }
            while (addedIterator.hasNext()) {
                final Component result = addedIterator.next();
                if (componentsToRemove.contains(result.getClass()))
                    continue;
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

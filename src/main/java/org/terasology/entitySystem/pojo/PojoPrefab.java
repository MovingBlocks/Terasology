/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.entitySystem.AbstractPrefab;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.metadata.ComponentLibrary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefab extends AbstractPrefab implements Prefab {

    private ComponentLibrary componentLibrary;
    private Map<Class<? extends Component>, Component> components;

    private List<Prefab> parents;

    private boolean persisted = true;

    private transient Map<Class<? extends Component>, Component> componentCache;

    protected PojoPrefab(String name, ComponentLibrary componentLibrary) {
        this(name, componentLibrary, Maps.<Class<? extends Component>, Component>newHashMap(), Lists.<Prefab>newLinkedList());
    }

    protected PojoPrefab(String name, ComponentLibrary componentLibrary, Map<Class<? extends Component>, Component> components, List<Prefab> parents) {
        super(name);
        this.componentLibrary = componentLibrary;

        this.components = components;
        this.parents = parents;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        checkComponentCache();

        return componentCache.containsKey(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        checkComponentCache();

        return componentClass.cast(componentCache.get(componentClass));
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        // Update data
        this.components.put(component.getClass(), component);

        if (componentCache != null) {
            this.componentCache.put(component.getClass(), component);
        }

        return component;
    }

    public void removeComponent(Class<? extends Component> componentClass) {
        components.remove(componentClass);
        this.invalidateComponentCache();
    }

    @Override
    public void saveComponent(Component component) {
        addComponent(component);
    }

    public Iterable<Component> iterateComponents() {
        checkComponentCache();

        return Collections.unmodifiableCollection(componentCache.values());
    }

    public Iterable<Component> iterateOwnedComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    public void addParent(Prefab parent) {
        if (this.parents.contains(parent)) {
            // @todo should we throw IllegalArgumentException instead?
            return;
        }

        this.parents.add(parent);
        this.invalidateComponentCache();
    }

    public Iterable<Prefab> getParents() {
        return Collections.unmodifiableCollection(parents);
    }

    public void removeParent(Prefab parent) {
        this.parents.remove(parent);
        invalidateComponentCache();
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    private void checkComponentCache() {
        if (components == null) {
            throw new IllegalStateException("Prefab is destroyed!");
        }

        if (componentCache == null) {
            this.buildComponentCache();
        }
    }

    private void invalidateComponentCache() {
        componentCache = null;
    }

    private void buildComponentCache() {
        // save old cache, will find changes from it
        Map<Class<? extends Component>, Component> oldCache = componentCache;

        componentCache = Maps.newHashMap();

        // 1) Fill inherited components
        for (Prefab ref : this.getParents()) {
            for (Component component : ref.iterateComponents()) {
                componentCache.put(component.getClass(), componentLibrary.copy(component));
            }
        }

        // 2) Find delta changes
        if (oldCache != null) {
            for (Component component : oldCache.values()) {
                if (!component.equals(componentCache.get(component.getClass()))) {
                    components.put(component.getClass(), component);
                }
            }
        }

        // 3) Fill own components
        for (Component component : components.values()) {
            componentCache.put(component.getClass(), component);
        }

        // 4) PROFIT!
    }

}

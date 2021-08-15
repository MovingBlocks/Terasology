// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity;

import com.google.common.collect.Maps;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.entitySystem.prefab.Prefab;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An entity store provides the ability to set up an entity before creating it.
 */
public class EntityStore implements MutableComponentContainer {

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private Prefab prefab;

    public EntityStore() {
        // no prefab
    }

    /**
     * @param prefab
     */
    public EntityStore(Prefab prefab) {
        this.prefab = prefab;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.keySet().contains(component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        return !Collections.disjoint(components.keySet(), filterComponents);
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        return components.keySet().containsAll(filterComponents);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        components.put(component.getClass(), component);
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        components.remove(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        components.put(component.getClass(), component);
    }

    public Prefab getPrefab() {
        return prefab;
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return components.values();
    }
}

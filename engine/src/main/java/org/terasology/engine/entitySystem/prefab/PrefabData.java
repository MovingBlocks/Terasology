// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab;

import com.google.common.collect.Maps;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.MutableComponentContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrefabData implements MutableComponentContainer, AssetData {

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private boolean persisted = true;
    private Prefab parent;
    private boolean alwaysRelevant;

    public static PrefabData createFromPrefab(Prefab prefab) {
        PrefabData result = new PrefabData();
        for (Component component : prefab.iterateComponents()) {
            result.addComponent(component);
        }

        result.setAlwaysRelevant(prefab.isAlwaysRelevant());
        result.setParent(prefab.getParent());
        result.setPersisted(prefab.isPersisted());
        return result;
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
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.containsKey(component);
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
    public Iterable<Component> iterateComponents() {
        return components.values();
    }

    public Map<Class<? extends Component>, Component> getComponents() {
        return components;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public void setParent(Prefab parent) {
        this.parent = parent;
    }

    public Prefab getParent() {
        return parent;
    }

    public boolean isAlwaysRelevant() {
        return alwaysRelevant;
    }

    public void setAlwaysRelevant(boolean alwaysRelevant) {
        this.alwaysRelevant = alwaysRelevant;
    }

}

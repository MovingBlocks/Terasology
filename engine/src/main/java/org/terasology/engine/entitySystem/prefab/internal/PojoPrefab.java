// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PojoPrefab extends Prefab {

    private Prefab parent;
    private Map<Class<? extends Component>, Component> componentMap;
    private List<Prefab> children = Lists.newArrayList();
    private boolean persisted;
    private boolean alwaysRelevant = true;

    public PojoPrefab(ResourceUrn urn, AssetType<?, PrefabData> assetType, PrefabData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    public Prefab getParent() {
        return parent;
    }

    @Override
    public List<Prefab> getChildren() {
        return ImmutableList.copyOf(children);
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public boolean isAlwaysRelevant() {
        return alwaysRelevant;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return componentMap.containsKey(component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        return !Collections.disjoint(componentMap.keySet(), filterComponents);
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        return componentMap.keySet().containsAll(filterComponents);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(componentMap.get(componentClass));
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return ImmutableList.copyOf(componentMap.values());
    }

    @Override
    protected void doReload(PrefabData data) {
        this.componentMap = ImmutableMap.copyOf(data.getComponents());
        this.persisted = data.isPersisted();
        this.alwaysRelevant = data.isAlwaysRelevant();
        this.parent = data.getParent();
        if (parent instanceof PojoPrefab) {
            ((PojoPrefab) parent).children.add(this);
        }
    }

}

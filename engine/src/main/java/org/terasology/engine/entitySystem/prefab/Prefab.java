// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.entitySystem.ComponentContainer;

import java.util.List;

/**
 * An entity prefab describes the recipe for creating an entity.
 * Like an entity it groups a collection of components.
 *
 */
public abstract class Prefab extends Asset<PrefabData> implements ComponentContainer {

    protected Prefab(ResourceUrn urn, AssetType<?, PrefabData> assetType) {
        super(urn, assetType);
    }

    public final String getName() {
        return getUrn().toString();
    }

    /**
     * Return parents prefabs
     *
     * @return
     */
    public abstract Prefab getParent();

    public abstract List<Prefab> getChildren();

    public abstract boolean isPersisted();

    public abstract boolean isAlwaysRelevant();

    public abstract boolean exists();

    @Override
    public String toString() {
        return "Prefab(" + getUrn() + "){ components: " + this.iterateComponents() + ", parent: " + this.getParent() + " }";
    }

}

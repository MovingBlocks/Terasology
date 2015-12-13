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
package org.terasology.entitySystem.prefab;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.ComponentContainer;

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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab.internal;

import com.google.common.base.Strings;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Basic implementation of PrefabManager.
 *
 * @see PrefabManager
 */
public class PojoPrefabManager implements PrefabManager {

    private final AssetManager assetManager;

    public PojoPrefabManager(Context context) {
        this.assetManager = context.get(AssetManager.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Prefab getPrefab(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return null;
        }
        return assetManager.getAsset(name, Prefab.class).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return false;
        }
        return assetManager.getAsset(name, Prefab.class).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Prefab> listPrefabs() {
        return assetManager.getLoadedAssets(Prefab.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Prefab> listPrefabs(Class<? extends Component> comp) {
        return assetManager.getLoadedAssets(Prefab.class).stream().filter(p -> p.getComponent(comp) != null)
                .collect(Collectors.toCollection(HashSet::new));
    }
}

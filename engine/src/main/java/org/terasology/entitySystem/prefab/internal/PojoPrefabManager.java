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
package org.terasology.entitySystem.prefab.internal;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;

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

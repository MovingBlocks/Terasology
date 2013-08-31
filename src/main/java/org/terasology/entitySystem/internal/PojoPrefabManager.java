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
package org.terasology.entitySystem.internal;

import com.google.common.collect.Sets;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;

import java.util.Collection;

/**
 * Basic implementation of PrefabManager.
 *
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 * @see PrefabManager
 */
public class PojoPrefabManager implements PrefabManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public Prefab getPrefab(String name) {
        return Assets.getPrefab(name);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String name) {
        return Assets.getPrefab(name) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Prefab> listPrefabs() {
        return CoreRegistry.get(AssetManager.class).listLoadedAssets(AssetType.PREFAB, Prefab.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Prefab> listPrefabs(Class<? extends Component> comp) {
        Collection<Prefab> prefabs = Sets.newHashSet();

        for (Prefab p : CoreRegistry.get(AssetManager.class).listLoadedAssets(AssetType.PREFAB, Prefab.class)) {
            if (p.getComponent(comp) != null) {
                prefabs.add(p);
            }
        }

        return prefabs;
    }
}

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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Basic implementation of PrefabManager.
 *
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 * @see PrefabManager
 */
public class PojoPrefabManager implements PrefabManager {

    /**
     * Map that stores the loaded Prefabs.
     */
    private Map<String, Prefab> prefabTable = Maps.newHashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    public Prefab getPrefab(String name) {
        String normalisedName = normalizeName(name);
        return exists(normalisedName) ? prefabTable.get(normalisedName) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String name) {
        String normalisedName = normalizeName(name);
        return prefabTable.containsKey(normalisedName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Prefab registerPrefab(Prefab prefab) {
        String normalisedName = normalizeName(prefab.getName());
        if (prefabTable.containsKey(normalisedName)) {
            throw new IllegalArgumentException("Prefab '" + prefab.getName() + "' already registered!");
        }

        prefabTable.put(normalisedName, prefab);

        return prefab;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Prefab> listPrefabs() {
        return prefabTable.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Prefab> listPrefabs(Class<? extends Component> comp) {
        Collection<Prefab> prefabs = Sets.newHashSet();

        for (Prefab p : prefabTable.values()) {
            if (p.getComponent(comp) != null) {
                prefabs.add(p);
            }
        }

        return prefabs;
    }

    private String normalizeName(String name) {
        return name.toLowerCase(Locale.ENGLISH);
    }
}

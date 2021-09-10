// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;

import java.util.Set;

/**
 * Suggegests prefabs.
 */
public final class PrefabSuggester implements CommandParameterSuggester<Prefab> {
    private final AssetManager assetManager;

    public PrefabSuggester(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public Set<Prefab> suggest(EntityRef sender, Object... resolvedParameters) {
        Iterable<Prefab> loadedPrefabs = assetManager.getLoadedAssets(Prefab.class);

        return Sets.newHashSet(loadedPrefabs);
    }
}

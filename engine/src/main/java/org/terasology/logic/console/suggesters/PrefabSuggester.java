/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.registry.CoreRegistry;

import java.util.Set;

/**
 * @author Limeth
 */
public class PrefabSuggester implements CommandParameterSuggester<Prefab> {
    @Override
    public Set<Prefab> suggest(EntityRef sender, Object... resolvedParameters) {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        Iterable<Prefab> loadedPrefabs = assetManager.listLoadedAssets(AssetType.PREFAB, Prefab.class);

        return Sets.newHashSet(loadedPrefabs);
    }
}

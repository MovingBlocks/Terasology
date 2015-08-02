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
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.world.block.loader.BlockFamilyDefinition;

import java.util.Set;

/**
 * Suggests block families.
 */
public class BlockFamilySuggester implements CommandParameterSuggester<ResourceUrn> {
    private final AssetManager assetManager;

    public BlockFamilySuggester(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public Set<ResourceUrn> suggest(EntityRef sender, Object... resolvedParameters) {
        Iterable<ResourceUrn> iterable = assetManager.getAvailableAssets(BlockFamilyDefinition.class);

        return Sets.newHashSet(iterable);
    }
}

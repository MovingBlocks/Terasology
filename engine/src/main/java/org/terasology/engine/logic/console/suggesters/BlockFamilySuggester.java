// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;

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

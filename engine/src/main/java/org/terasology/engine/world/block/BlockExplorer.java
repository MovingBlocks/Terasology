// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import org.terasology.assets.management.AssetManager;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class BlockExplorer {
    private AssetManager assetManager;

    public BlockExplorer(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Set<BlockUri> getAvailableBlockFamilies() {
        return assetManager.getAvailableAssets(BlockFamilyDefinition.class)
                .stream().<Optional<BlockFamilyDefinition>>map(urn -> assetManager.getAsset(urn, BlockFamilyDefinition.class))
                .filter(def -> def.isPresent() && def.get().isLoadable() && !def.get().isFreeform())
                .map(r -> new BlockUri(r.get().getUrn())).collect(Collectors.toSet());
    }

    public Set<BlockUri> getFreeformBlockFamilies() {
        return assetManager.getAvailableAssets(BlockFamilyDefinition.class)
                .stream().<Optional<BlockFamilyDefinition>>map(urn -> assetManager.getAsset(urn, BlockFamilyDefinition.class))
                .filter(def -> def.isPresent() && def.get().isLoadable() && def.get().isFreeform())
                .map(r -> new BlockUri(r.get().getUrn())).collect(Collectors.toSet());
    }
}

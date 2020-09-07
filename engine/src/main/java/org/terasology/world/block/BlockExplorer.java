/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.block;

import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.world.block.loader.BlockFamilyDefinition;

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

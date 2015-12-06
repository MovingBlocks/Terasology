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
package org.terasology.world.block.loader;

import com.google.common.collect.Sets;
import org.terasology.assets.AssetDataProducer;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.naming.Name;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.family.FreeformBlockFamilyFactory;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.block.tiles.BlockTile;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 */
@RegisterAssetDataProducer
public class AutoBlockProvider implements AssetDataProducer<BlockFamilyDefinitionData> {

    private AssetManager assetManager;
    private FreeformBlockFamilyFactory freeformBlockFamilyFactory;

    public AutoBlockProvider(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.freeformBlockFamilyFactory = new FreeformBlockFamilyFactory();
    }

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        Set<ResourceUrn> result = Sets.newLinkedHashSet();

        assetManager.getAvailableAssets(BlockTile.class).stream()
                .map(urn -> assetManager.getAsset(urn, BlockTile.class).get())
                .filter(BlockTile::isAutoBlock)
                .forEach(tile -> result.add(tile.getUrn()));
        return result;
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        Set<Name> result = Sets.newLinkedHashSet();
        assetManager.resolve(resourceName.toString(), BlockTile.class).stream()
                .map(urn -> assetManager.getAsset(urn, BlockTile.class).get())
                .filter(BlockTile::isAutoBlock)
                .forEach(tile -> result.add(tile.getUrn().getResourceName()));
        return result;
    }

    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    @Override
    public Optional<BlockFamilyDefinitionData> getAssetData(ResourceUrn urn) throws IOException {
        Optional<BlockTile> blockTile = assetManager.getAsset(urn, BlockTile.class);
        if (blockTile.isPresent() && blockTile.get().isAutoBlock()) {
            BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
            for (BlockPart part : BlockPart.values()) {
                data.getBaseSection().getBlockTiles().put(part, blockTile.get());
            }
            data.getBaseSection().setSounds(assetManager.getAsset("engine:default", BlockSounds.class).get());
            data.setFamilyFactory(freeformBlockFamilyFactory);
            return Optional.of(data);
        }
        return Optional.empty();
    }
}

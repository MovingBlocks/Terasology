// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.loader;

import com.google.common.collect.Sets;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.world.block.BlockPart;
import org.terasology.engine.world.block.family.FreeformFamily;
import org.terasology.engine.world.block.tiles.BlockTile;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@RegisterAssetDataProducer
public class AutoBlockProvider implements AssetDataProducer<BlockFamilyDefinitionData> {

    private AssetManager assetManager;

    public AutoBlockProvider(AssetManager assetManager) {
        this.assetManager = assetManager;
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
                .forEach(tile -> result.add(tile.getUrn().getModuleName()));
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
            data.setBlockFamily(FreeformFamily.class);
            return Optional.of(data);
        }
        return Optional.empty();
    }
}

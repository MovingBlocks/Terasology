// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.loader;

import com.google.common.base.Preconditions;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.context.annotation.API;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.BlockFamilyLibrary;

import java.util.Collections;
import java.util.List;

@API
public class BlockFamilyDefinition extends Asset<BlockFamilyDefinitionData> {

    private BlockFamilyDefinitionData data;

    public BlockFamilyDefinition(ResourceUrn urn, AssetType<?, BlockFamilyDefinitionData> assetType, BlockFamilyDefinitionData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(BlockFamilyDefinitionData blockFamilyDefinitionData) {
        this.data = blockFamilyDefinitionData;
    }

    public List<String> getCategories() {
        return Collections.unmodifiableList(data.getCategories());
    }

    public boolean isFreeform() {
        return BlockFamilyLibrary.isFreeformSupported(getData().getBlockFamily());
    }

    public BlockFamily createFamily(BlockBuilderHelper blockBuilderHelper) {
        Preconditions.checkState(!isFreeform());
        return BlockFamilyLibrary.createFamily(getData().getBlockFamily(), this, blockBuilderHelper);
    }

    public BlockFamily createFamily(BlockShape shape, BlockBuilderHelper blockBuilderHelper) {
        Preconditions.checkState(isFreeform());
        return BlockFamilyLibrary.createFamily(getData().getBlockFamily(), this, shape, blockBuilderHelper);
    }


    public BlockFamilyDefinitionData getData() {
        return new BlockFamilyDefinitionData(data);
    }

    public boolean isLoadable() {
        return getData().isValid() && !getData().isTemplate();
    }
}

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
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.BlockTile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Immortius
 */
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

    @Override
    protected void doDispose() {
        data = null;
    }

    public List<String> getCategories() {
        return Collections.unmodifiableList(data.getCategories());
    }

    public boolean isFreeform() {
        Set<BlockTile> tileSet = Sets.newHashSet((getData().getBaseSection().getBlockTiles().values()));
        return getData().getBaseSection().getShape() == null && getData().getFamilyFactory() == null && !getData().getBaseSection().isLiquid()
                && tileSet.size() == 1;
    }

    public BlockFamily createFamily(BlockBuilderHelper blockBuilderHelper) {
        return getData().getFamilyFactory().createBlockFamily(this, blockBuilderHelper);
    }

    public BlockFamily createFamily(BlockShape shape, BlockBuilderHelper blockBuilderHelper) {
        return getData().getFamilyFactory().createBlockFamily(this, shape, blockBuilderHelper);
    }

    public BlockFamilyDefinitionData getData() {
        return new BlockFamilyDefinitionData(data);
    }

}

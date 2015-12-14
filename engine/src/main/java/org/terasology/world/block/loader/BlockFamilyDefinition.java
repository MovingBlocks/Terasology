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

import com.google.common.base.Preconditions;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Collections;
import java.util.List;

/**
 */
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
        return getData().getFamilyFactory().isFreeformSupported();
    }

    public BlockFamily createFamily(BlockBuilderHelper blockBuilderHelper) {
        Preconditions.checkState(!isFreeform());
        return getData().getFamilyFactory().createBlockFamily(this, blockBuilderHelper);
    }

    public BlockFamily createFamily(BlockShape shape, BlockBuilderHelper blockBuilderHelper) {
        Preconditions.checkState(isFreeform());
        return getData().getFamilyFactory().createBlockFamily(this, shape, blockBuilderHelper);
    }


    public BlockFamilyDefinitionData getData() {
        return new BlockFamilyDefinitionData(data);
    }

    public boolean isLoadable() {
        return getData().isValid() && !getData().isTemplate();
    }
}

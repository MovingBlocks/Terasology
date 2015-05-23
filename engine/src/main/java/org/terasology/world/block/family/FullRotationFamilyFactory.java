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
package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rotation;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

/**
 * Block families created with this factory will contain a full set of rotations for the block.
 */
@RegisterBlockFamilyFactory("fullRotation")
public class FullRotationFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Rotation, Block> blocksByRotation = Maps.newHashMap();
        for (Rotation rotation : Rotation.values()) {
            blocksByRotation.put(rotation, blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, rotation));
        }

        BlockUri familyUri = new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName());
        return new FullRotationFamily(familyUri, blockDefinition.categories, Rotation.none(), blocksByRotation);
    }
}

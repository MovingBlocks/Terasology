/*
 * Copyright 2013 Moving Blocks
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

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.EnumMap;
import java.util.Map;

@RegisterBlockFamilyFactory("allSides")
public class AllSidesFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Side, Block> blocksBySide = new EnumMap<Side, Block>(Side.class);
        final Block frontBlock = blockBuilder.constructSimpleBlock(blockDefUri, blockDefinition);
        blocksBySide.put(Side.FRONT, frontBlock);
        blocksBySide.put(Side.LEFT, blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, Rotation.rotate(Yaw.CLOCKWISE_90)));
        blocksBySide.put(Side.BACK, blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, Rotation.rotate(Yaw.CLOCKWISE_180)));
        blocksBySide.put(Side.RIGHT, blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, Rotation.rotate(Yaw.CLOCKWISE_270)));
        blocksBySide.put(Side.TOP, blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, Rotation.rotate(Pitch.CLOCKWISE_90)));
        blocksBySide.put(Side.BOTTOM, blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, Rotation.rotate(Pitch.CLOCKWISE_270)));
        return new AllSidesFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockDefinition.categories, blocksBySide.get(Side.LEFT), blocksBySide);
    }
}

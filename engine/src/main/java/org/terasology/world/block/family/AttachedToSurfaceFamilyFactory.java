/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

@RegisterBlockFamilyFactory("attachedToSurface")
public class AttachedToSurfaceFamilyFactory implements BlockFamilyFactory {

    private static final String TOP = "top";
    private static final String SIDES = "sides";
    private static final String BOTTOM = "bottom";

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        BlockDefinition topDef = blockBuilder.getBlockDefinitionForSection(blockDefJson, TOP);
        if (topDef != null) {
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, topDef);
            block.setDirection(Side.TOP);
            blockMap.put(Side.TOP, block);
        }
        BlockDefinition sideDef = blockBuilder.getBlockDefinitionForSection(blockDefJson, SIDES);
        if (sideDef != null) {
            blockMap.putAll(blockBuilder.constructHorizontalRotatedBlocks(blockDefUri, sideDef));
        }
        BlockDefinition bottomDef = blockBuilder.getBlockDefinitionForSection(blockDefJson, BOTTOM);
        if (bottomDef != null) {
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, bottomDef);
            block.setDirection(Side.BOTTOM);
            blockMap.put(Side.BOTTOM, block);
        }
        return new AttachedToSurfaceFamily(new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName()), blockMap, blockDefinition.categories);
    }

}

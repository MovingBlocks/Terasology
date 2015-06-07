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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RegisterBlockFamilyFactory("attachedToSurface")
public class AttachedToSurfaceFamilyFactory implements BlockFamilyFactory {

    private static final ImmutableSet<String> BLOCK_NAMES = ImmutableSet.of("front", "left", "right", "back", "top", "bottom");
    private static final ImmutableList<MultiSection> MULTI_SECTIONS = ImmutableList.of(
            new MultiSection("all", "front", "left", "right", "back", "top", "bottom"),
            new MultiSection("topBottom", "top", "bottom"),
            new MultiSection("sides", "front", "left", "right", "back"));

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        if (definition.getData().hasSection("top")) {
            Block block = blockBuilder.constructSimpleBlock(definition, "top");
            block.setDirection(Side.TOP);
            blockMap.put(Side.TOP, block);
        }
        if (definition.getData().hasSection("front")) {
            for (Rotation rot : Rotation.horizontalRotations()) {
                Side side = rot.rotate(Side.FRONT);
                blockMap.put(side, blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot));
            }
        }
        if (definition.getData().hasSection("bottom")) {
            Block block = blockBuilder.constructSimpleBlock(definition, "bottom");
            block.setDirection(Side.BOTTOM);
            blockMap.put(Side.BOTTOM, block);
        }
        return new AttachedToSurfaceFamily(new BlockUri(definition.getUrn()), blockMap, definition.getCategories());
    }

    @Override
    public Set<String> getSectionNames() {
        return BLOCK_NAMES;
    }

    @Override
    public ImmutableList<MultiSection> getMultiSections() {
        return MULTI_SECTIONS;
    }
}

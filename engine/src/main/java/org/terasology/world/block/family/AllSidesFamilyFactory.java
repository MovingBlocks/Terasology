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
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import org.terasology.math.Pitch;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.Yaw;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RegisterBlockFamilyFactory("allSides")
public class AllSidesFamilyFactory implements BlockFamilyFactory {
    private static final ImmutableSet<String> BLOCK_NAMES = ImmutableSet.of("front", "left", "right", "back", "top", "bottom");
    private static final ImmutableList<MultiSection> MULTI_SECTIONS = ImmutableList.of(
            new MultiSection("all", "front", "left", "right", "back", "top", "bottom"),
            new MultiSection("topBottom", "top", "bottom"),
            new MultiSection("sides", "front", "left", "right", "back"));

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        Map<Side, Block> blocksBySide = new EnumMap<>(Side.class);
        blocksBySide.put(Side.FRONT, blockBuilder.constructSimpleBlock(definition, "front"));
        blocksBySide.put(Side.LEFT, blockBuilder.constructTransformedBlock(definition, "left", Rotation.rotate(Yaw.CLOCKWISE_90)));
        blocksBySide.put(Side.BACK, blockBuilder.constructTransformedBlock(definition, "back", Rotation.rotate(Yaw.CLOCKWISE_180)));
        blocksBySide.put(Side.RIGHT, blockBuilder.constructTransformedBlock(definition, "right", Rotation.rotate(Yaw.CLOCKWISE_270)));
        blocksBySide.put(Side.TOP, blockBuilder.constructTransformedBlock(definition, "top", Rotation.rotate(Pitch.CLOCKWISE_90)));
        blocksBySide.put(Side.BOTTOM, blockBuilder.constructTransformedBlock(definition, "bottom", Rotation.rotate(Pitch.CLOCKWISE_270)));
        BlockUri familyUri = new BlockUri(definition.getUrn());
        return new AllSidesFamily(familyUri, definition.getCategories(), blocksBySide.get(Side.LEFT), blocksBySide);
    }

    @Override
    public Set<String> getSectionNames() {
        return BLOCK_NAMES;
    }

    @Override
    public List<MultiSection> getMultiSections() {
        return MULTI_SECTIONS;
    }
}

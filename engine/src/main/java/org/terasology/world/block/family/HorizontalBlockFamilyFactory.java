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
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RegisterBlockFamilyFactory("horizontal")
public class HorizontalBlockFamilyFactory implements BlockFamilyFactory {
    private static final ImmutableSet<String> BLOCK_NAMES = ImmutableSet.of("front", "left", "right", "back", "top", "bottom");
    private static final ImmutableList<MultiSection> MULTI_SECTIONS = ImmutableList.of(
            new MultiSection("all", "front", "left", "right", "back", "top", "bottom"),
            new MultiSection("topBottom", "top", "bottom"),
            new MultiSection("sides", "front", "left", "right", "back"));

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        if (definition.isFreeform()) {
            throw new IllegalStateException("A shape must be provided when creating a family for a freeform block family definition");
        }
        Map<Side, Block> blockMap = Maps.newHashMap();
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);
            blockMap.put(side, blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot));
        }

        return new HorizontalBlockFamily(new BlockUri(definition.getUrn()), getArchetypeSide(), blockMap, definition.getCategories());
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        if (!definition.isFreeform()) {
            throw new IllegalStateException("A shape cannot be provided when creating a family for a non-freeform block family definition");
        }
        Map<Side, Block> blockMap = Maps.newHashMap();
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);
            blockMap.put(side, blockBuilder.constructTransformedBlock(definition, shape, side.toString().toLowerCase(Locale.ENGLISH), rot));
        }

        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        return new HorizontalBlockFamily(uri, getArchetypeSide(), blockMap, definition.getCategories());
    }

    @Override
    public Set<String> getSectionNames() {
        return BLOCK_NAMES;
    }

    @Override
    public ImmutableList<MultiSection> getMultiSections() {
        return MULTI_SECTIONS;
    }

    @Override
    public boolean isFreeformSupported() {
        return false;
    }
}

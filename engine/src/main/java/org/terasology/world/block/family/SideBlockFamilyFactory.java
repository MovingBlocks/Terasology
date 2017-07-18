/*
 * Copyright 2017 MovingBlocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Pitch;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.naming.Name;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;


@RegisterBlockFamilyFactory("side")
public class SideBlockFamilyFactory implements BlockFamilyFactory {
    private static Logger logger = LoggerFactory.getLogger(SideBlockFamilyFactory.class);

    private static EnumMap<Side, Rotation> rotations;

    static {
        rotations = new EnumMap<Side, Rotation>(Side.class);
        rotations.put(Side.BOTTOM, Rotation.none());
        rotations.put(Side.RIGHT, Rotation.rotate(Roll.CLOCKWISE_90));
        rotations.put(Side.TOP, Rotation.rotate(Roll.CLOCKWISE_180));
        rotations.put(Side.LEFT, Rotation.rotate(Roll.CLOCKWISE_270));
        rotations.put(Side.FRONT, Rotation.rotate(Pitch.CLOCKWISE_90));
        rotations.put(Side.BACK, Rotation.rotate(Pitch.CLOCKWISE_270));
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        throw new IllegalStateException("A shape must be provided when creating a family for a side block family definition");
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        if (!definition.isFreeform()) {
            throw new IllegalStateException("A shape cannot be provided when creating a family for a non-freeform block family definition");
        }
        BlockUri uri = new BlockUri(definition.getUrn(), shape.getUrn());

        Side archetypeSide = Side.valueOf(shape.getBlockShapePlacement().getArchetype().toUpperCase());

        boolean symmetric = shape.getBlockShapePlacement().getSymmetry().hasSideSymmetry();

        EnumMap<Side, Block> blockMap = new EnumMap<Side, Block>(Side.class);
        for (Side side : Side.values()) {

            Block block = blockBuilder.constructTransformedBlock(definition, shape, side.toString(), rotations.get(side));
            block.setUri(new BlockUri(uri, new Name(side.toString())));
            blockMap.put(side, block);
        }

        return new SideBlockFamily(uri, archetypeSide, blockMap, definition.getCategories());
    }

    @Override
    public Set<String> getSectionNames() {
        return Collections.emptySet();
    }

    @Override
    public ImmutableList<MultiSection> getMultiSections() {
        return ImmutableList.of();
    }

    @Override
    public boolean isFreeformSupported() {
        return false;
    }
}

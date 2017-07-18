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
import org.terasology.math.Corner;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;


@RegisterBlockFamilyFactory("corner")
public class CornerBlockFamilyFactory implements BlockFamilyFactory {
    private static Logger logger = LoggerFactory.getLogger(CornerBlockFamilyFactory.class);

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        throw new IllegalStateException("A shape must be provided when creating a family for a corner block family definition");
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        if (!definition.isFreeform()) {
            throw new IllegalStateException("A shape cannot be provided when creating a family for a non-freeform block family definition");
        }
        BlockUri uri = new BlockUri(definition.getUrn(), shape.getUrn());

        Corner archetypeCorner = Corner.of(shape.getBlockShapePlacement().getArchetype());

        boolean symmetric = shape.getBlockShapePlacement().getSymmetry().hasCornerSymmetry();

        EnumMap<Corner, Block> blockMap = new EnumMap<Corner, Block>(Corner.class);
        for (Corner corner : Corner.symmetricSubset()) {
            Block block;
            block = blockBuilder.constructTransformedBlock(definition, shape, corner.toString(), corner.getRotationFromBottomLeftBack());
            block.setUri(new BlockUri(uri, corner.getName()));
            blockMap.put(corner, block);
        }

        for (Corner corner : Corner.values()) {
            Block block = null;
            if (symmetric) {
                block = blockMap.get(corner.getSymmetricEquivalent());
            } else {
                block = blockMap.get(corner);
                if (block == null) {
                    block = blockBuilder.constructTransformedBlock(definition, shape, corner.toString(), corner.getRotationFromBottomLeftBack());
                    block.setUri(new BlockUri(uri, corner.getName()));
                }
            }
            blockMap.put(corner, block);
        }

        return new CornerBlockFamily(uri, archetypeCorner, blockMap, definition.getCategories());
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

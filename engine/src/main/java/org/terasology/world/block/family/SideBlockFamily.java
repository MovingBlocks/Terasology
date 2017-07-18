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


import org.terasology.math.BlockHitDetector;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.EnumMap;

/**
 * Block group for blocks that can be oriented on 6 sides.
 */
public class SideBlockFamily extends AbstractBlockFamily {

    private EnumMap<Side, Block> blocks;
    private Side archetypeSide;

    public SideBlockFamily(BlockUri uri, Side archetypeSide, EnumMap<Side, Block> blocks, Iterable<String> categories) {
        super(uri, categories);
        this.archetypeSide = archetypeSide;
        this.blocks = blocks;
        for (Side side : Side.values()) {
            Block block = blocks.get(side);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            block.setBlockFamily(this);
        }
    }

    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Vector3f direction, Vector3f hitNormal, Vector3f hitPosition) {
        Side side = BlockHitDetector.detectSide(hitPosition, location);
        Block b = blocks.get(side);
        if (b == null) {
            b = getArchetypeBlock();
        }
        return b;
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        return getArchetypeBlock();
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(archetypeSide);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.of(blockUri.getIdentifier());
                return blocks.get(side);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }

}

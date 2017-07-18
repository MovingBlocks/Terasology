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
import org.terasology.math.Corner;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.EnumMap;

/**
 * Block group for blocks that can be oriented on 8 corners.
 */
public class CornerBlockFamily extends AbstractBlockFamily {

    private EnumMap<Corner, Block> blocks;
    private Corner archetypeCorner;

    public CornerBlockFamily(BlockUri uri, Corner archetypeCorner, EnumMap<Corner, Block> blocks, Iterable<String> categories) {
        super(uri, categories);
        this.archetypeCorner = archetypeCorner;
        this.blocks = blocks;
        for (Corner corner : Corner.values()) {
            Block block = blocks.get(corner);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for corner: " + corner.toString());
            }
            block.setBlockFamily(this);
        }
    }

    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Vector3f direction, Vector3f hitNormal, Vector3f hitPosition) {
        Corner corner = BlockHitDetector.detectCorner(hitPosition, location);
        Block b = blocks.get(corner);
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
        return blocks.get(archetypeCorner);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Corner corner = Corner.of(blockUri.getIdentifier());
                return blocks.get(corner);
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

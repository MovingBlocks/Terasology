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

import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.Locale;
import java.util.Map;

public class AllSidesFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {
    private Block archetypeBlock;
    private Map<Side, Block> sideBlocks;

    public AllSidesFamily(BlockUri uri, Iterable<String> categories, Block archetypeBlock, Map<Side, Block> sideBlocks) {
        super(uri, categories);

        for (Map.Entry<Side, Block> blockBySide : sideBlocks.entrySet()) {
            final Side side = blockBySide.getKey();
            final Block block = blockBySide.getValue();
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            block.setBlockFamily(this);
            block.setUri(new BlockUri(uri, side.name()));
        }

        this.archetypeBlock = archetypeBlock;
        this.sideBlocks = sideBlocks;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        return sideBlocks.get(Side.FRONT);
    }

    @Override
    public Block getBlockForSide(Side side) {
        return sideBlocks.get(side);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toUpperCase(Locale.ENGLISH));
                return sideBlocks.get(side);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return sideBlocks.values();
    }
}

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
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.Locale;
import java.util.Map;

/**
 * Block group for blocks that can be oriented around the vertical axis.
 *
 */
public class HorizontalBlockFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {

    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);
    private Side archetypeSide;

    /**
     * @param uri        The asset uri for the block group.
     * @param blocks     The set of blocks that make up the group. Front, Back, Left and Right must be provided - the rest is ignored.
     * @param categories The set of categories this block family belongs to
     */
    public HorizontalBlockFamily(BlockUri uri, Map<Side, Block> blocks, Iterable<String> categories) {
        this(uri, Side.FRONT, blocks, categories);
    }

    public HorizontalBlockFamily(BlockUri uri, Side archetypeSide, Map<Side, Block> blocks, Iterable<String> categories) {
        super(uri, categories);
        this.archetypeSide = archetypeSide;
        for (Side side : Side.horizontalSides()) {
            Block block = blocks.get(side);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            this.blocks.put(side, block);
            block.setBlockFamily(this);
            block.setUri(new BlockUri(uri, new Name(side.name())));
        }
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        if (attachmentSide.isHorizontal()) {
            return blocks.get(attachmentSide);
        }
        if (direction != null) {
            return blocks.get(direction);
        } else {
            return blocks.get(Side.FRONT);
        }
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(archetypeSide);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toString().toUpperCase(Locale.ENGLISH));
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

    @Override
    public Block getBlockForSide(Side side) {
        return blocks.get(side);
    }

    @Override
    public Side getSide(Block block) {
        for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
            if (block == sideBlockEntry.getValue()) {
                return sideBlockEntry.getKey();
            }
        }
        return null;
    }
}

/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import com.google.common.collect.Maps;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class AlignToSurfaceFamily extends AbstractBlockFamily {
    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);
    private Block archetype;

    /**
     * @param uri   The uri for the block group.
     * @param blocks The set of blocks that make up the group. Front, Back, Left and Right must be provided - the rest is ignored.
     */
    public AlignToSurfaceFamily(BlockUri uri, Map<Side, Block> blocks, String ... categories) {
        super(uri, Arrays.asList(categories));
        for (Side side : Side.values()) {
            Block block = blocks.get(side);
            if (block != null) {
                this.blocks.put(side, block);
                block.setBlockFamily(this);
                block.setUri(new BlockUri(uri, side.name()));
            }
        }
        if (this.blocks.containsKey(Side.TOP)) {
            archetype = this.blocks.get(Side.TOP);
        } else {
            archetype = this.blocks.get(Side.FRONT);
        }
    }

    @Override
    public Block getBlockForPlacing(WorldProvider worldProvider, Vector3i blockLocation, Side attachmentSide, Side direction) {
        return blocks.get(attachmentSide);
    }

    @Override
    public Block getBlockAfterNeighborUpdate(WorldProvider worldProvider, Vector3i blockLocation, Block currentBlock) {
        return currentBlock;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetype;
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toUpperCase(Locale.ENGLISH));
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

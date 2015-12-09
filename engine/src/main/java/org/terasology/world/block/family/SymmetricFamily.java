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
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.Arrays;
import java.util.Collections;

/**
 * The standard block group consisting of a single symmetrical block that doesn't need rotations
 *
 */
public class SymmetricFamily extends AbstractBlockFamily {

    private Block block;

    public SymmetricFamily(BlockUri uri, Block block) {
        this(uri, block, Collections.<String>emptyList());
    }

    public SymmetricFamily(BlockUri uri, Block block, Iterable<String> categories) {
        super(uri, categories);
        this.block = block;
        block.setBlockFamily(this);
        block.setUri(uri);
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        return block;
    }

    @Override
    public Block getArchetypeBlock() {
        return block;
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri()) && blockUri.getIdentifier().isEmpty()) {
            return block;
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return Arrays.asList(block);
    }

}

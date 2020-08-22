/*
 * Copyright 2018 MovingBlocks
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

import org.joml.Vector3ic;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

/**
 * A collection of related blocks that are all distinct but can collapse to a single "family" block.
 * This will enable such effects as players picking up a block with one orientation and it grouping
 * with the same block with different orientations, and placing it in different directions.
 */
public interface BlockFamily {
    ResourceUrn CUBE_SHAPE_URN = new ResourceUrn("engine:cube");

    /**
     * @return The block uri for this family
     */
    BlockUri getURI();

    /**
     * @return A displayable name for the block
     */
    String getDisplayName();

    /**
     * Ask the block family to calculate where to place a new block when the block
     * family is the target block.
     *
     * @param blockPlacementData context data needed to make the placement decision.
     * @return the block and position
     */
    BlockPlacement calculateBlockPlacement(BlockPlacementData blockPlacementData);

    /**
     * @return The base block defining the block group. Can be used for orientation-irrelevant behaviours
     */
    Block getArchetypeBlock();

    /**
     * Resolves a block within this family
     *
     * @param blockUri
     * @return The requested block, or null
     */
    Block getBlockFor(BlockUri blockUri);

    /**
     * @return An iterator over the blocks in this family
     */
    Iterable<Block> getBlocks();

    /**
     * @return An iterator over the categories this block family belongs to
     */
    Iterable<String> getCategories();

    /**
     * The BlockFamily can determine if a replacingBlock can be
     * @param targetBlock       Block that would be replaced.
     * @param replacingBlock    Block that would replace the targetBlock.
     * @return whether the block family will permit the replacement
     */
    boolean canBlockReplace(Block targetBlock, Block replacingBlock);

    /**
     * @param category
     * @return Whether this block family belongs to the given category (case-insensitive)
     */
    boolean hasCategory(String category);
}

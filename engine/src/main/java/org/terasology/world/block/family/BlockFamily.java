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

/**
 * A collection of blocks that are all different rotations of the same core block.
 * This will enable such effects as players picking up a block with one orientation and it grouping
 * with the same block with different orientations, and placing it in different directions.
 *
 */
public interface BlockFamily {

    /**
     * @return The block uri for this family
     */
    BlockUri getURI();

    /**
     * @return A displayable name for the block
     */
    String getDisplayName();

    /**
     * Get the block that is appropriate for placement in the given situation
     *
     * @param worldProvider
     * @param blockEntityRegistry
     * @param location
     * @param attachmentSide      The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     * @param direction           A secondary direction after the attachment side that determines the facing of the block.   @return The appropriate block
     */
    Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction);

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
     * @param category
     * @return Whether this block family belongs to the given category (case-insensitive)
     */
    boolean hasCategory(String category);
}

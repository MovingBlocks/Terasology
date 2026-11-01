// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import org.terasology.context.annotation.IndexInherited;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockUri;

/**
 * A collection of related blocks that are all distinct but can collapse to a single "family" block.
 * This will enable such effects as players picking up a block with one orientation and it grouping
 * with the same block with different orientations, and placing it in different directions.
 */
@IndexInherited
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
     * Get the block that is appropriate for placement in the given situation,
     * which is determined by the provided block placement data.
     *
     * @param data block placement data
     * @return The appropriate block
     */
    Block getBlockForPlacement(BlockPlacementData data);

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

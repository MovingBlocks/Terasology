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
package org.terasology.world;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

/**
 * Manages creation and lookup of entities linked to blocks
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockEntityRegistry {

    /**
     * @param blockPosition
     * @return The block entity for the location if it exists, or the null entity
     */
    EntityRef getBlockEntityAt(Vector3i blockPosition);

    /**
     * @param blockPosition
     * @return The block entity for the location, creating it if it doesn't exist
     */
    EntityRef getOrCreateBlockEntityAt(Vector3i blockPosition);

    /**
     * @param blockPosition
     * @return The block controller entity for this location, or block entity if it exists.
     */
    EntityRef getEntityAt(Vector3i blockPosition);

    /**
     * @param blockPosition
     * @return The block controller entity for this location, or block entity.
     */
    EntityRef getOrCreateEntityAt(Vector3i blockPosition);

    /**
     * Replaces the entity at the given block position, destroying the old one (if it exists)
     * The entity will be given block-entity related components.
     *
     * @param blockPosition
     */
    void replaceEntityAt(Vector3i blockPosition, EntityRef entity);

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values. Additionally sets the entity for this block position if successful.
     * Block-entity related components will be added to the entity.
     *
     * @param x       The X-coordinate
     * @param y       The Y-coordinate
     * @param z       The Z-coordinate
     * @param type    The type of the block to set
     * @param oldType The old type of the block
     * @param entity  T
     * @return True if a block was set/replaced. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    public boolean setBlock(int x, int y, int z, Block type, Block oldType, EntityRef entity);

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values. Additionally sets the entity for this block position if successful.
     * Block-entity related components will be added to the entity.
     *
     * @param pos
     * @param type    The type of the block to set
     * @param oldType The old type of the block
     * @param entity  T
     * @return True if a block was set/replaced. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    public boolean setBlock(Vector3i pos, Block type, Block oldType, EntityRef entity);
}

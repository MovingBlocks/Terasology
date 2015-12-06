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
package org.terasology.world;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

/**
 * Manages creation and lookup of entities linked to blocks
 *
 */
public interface BlockEntityRegistry {

    /**
     * This method returns the block entity at the given location, but will not produce a temporary entity if
     * one isn't currently in memory.
     *
     * @param blockPosition
     * @return The block entity for the location if it exists, or the null entity
     */
    EntityRef getExistingBlockEntityAt(Vector3i blockPosition);

    /**
     * This method is the same as setBlock, except if the old and new block types are part of the same family the
     * entity will be force updated (usually they are not in this situation).
     *
     * @param position
     * @param type
     * @return The previous block type, or null if the change failed due to the chunk not being available
     */
    Block setBlockForceUpdateEntity(Vector3i position, Block type);

    /**
     * This method is the same as setBlock, except the specified components are not altered during the update
     *
     * @param position
     * @param type
     * @param components
     * @return The previous block type, or null if the change failed due to the chunk not being available
     */
    Block setBlockRetainComponent(Vector3i position, Block type, Class<? extends Component>... components);

    /**
     * @param position
     * @return The block entity for the location, creating it if it doesn't exist
     */
    EntityRef getBlockEntityAt(Vector3f position);

    /**
     * @param blockPosition
     * @return The block entity for the location, creating it if it doesn't exist
     */
    EntityRef getBlockEntityAt(Vector3i blockPosition);

    /**
     * @param blockPosition
     * @return The block controller entity for this location, or block entity if it exists.
     */
    EntityRef getExistingEntityAt(Vector3i blockPosition);

    /**
     * @param blockPosition
     * @return The block controller entity for this location, or block entity.
     */
    EntityRef getEntityAt(Vector3i blockPosition);

    /**
     * @param blockPos
     * @return Whether the entity at this position is permanent
     */
    boolean hasPermanentBlockEntity(Vector3i blockPos);
}

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
}

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
package org.terasology.world.block.items;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.joml.Vector3i;

/**
 * This event gets called whenever a block item is placed in the world
 */
public class OnBlockItemPlaced implements Event {
    /**
     * The position where the block is placed
     */
    private Vector3i position;

    /**
     *  The entity corresponding to the placed block
     */
    private EntityRef placedBlock;

    /**
     * The entity which placed the block
     */
    private EntityRef instigator = EntityRef.NULL;

    @Deprecated
    public OnBlockItemPlaced(Vector3i pos, EntityRef placedBlock) {
        this.position = pos;
        this.placedBlock = placedBlock;
    }

    public OnBlockItemPlaced(Vector3i pos, EntityRef placedBlock, EntityRef instigator) {
        this.position = pos;
        this.placedBlock = placedBlock;
        this.instigator = instigator;
    }
    public Vector3i getPosition() {
        return new Vector3i(position);
    }

    public EntityRef getPlacedBlock() {
        return placedBlock;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

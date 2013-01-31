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
package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.world.block.Block;

/**
 * copied from the attackevent
 * @author Immortius <immortius@gmail.com>
 * created as a way to catch minions breaking blocks and sending them to their inventory
 * oldblock is the block being destroyed, instigator is the entity who broke the block
 * droppedblock is the itemblock dropped in the world
 */
public class BlockDroppedEvent extends AbstractEvent {
    private EntityRef droppedblock;
    private Block oldblock;
    
    public BlockDroppedEvent(Block oldblock, EntityRef droppedblock) {
        this.oldblock = oldblock;
        this.droppedblock = droppedblock;
    }
    
    public EntityRef getDroppedBlock() {
        return droppedblock;
    }
    
    public Block getoldBlock() {
        return oldblock;
    }
}

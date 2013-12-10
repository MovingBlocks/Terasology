/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

/**
 * @author Immortius
 */
public class OnLeaveBlockEvent implements Event {
    private Block block;
    private Vector3i blockWorldPosition;
    private Vector3i characterRelativePosition;

    public OnLeaveBlockEvent(Block block, Vector3i blockWorldPosition, Vector3i characterRelativePosition) {
        this.block = block;
        this.blockWorldPosition = blockWorldPosition;
        this.characterRelativePosition = characterRelativePosition;
    }

    public Block getBlock() {
        return block;
    }

    public Vector3i getBlockWorldPosition() {
        return blockWorldPosition;
    }

    public Vector3i getCharacterRelativePosition() {
        return characterRelativePosition;
    }
}

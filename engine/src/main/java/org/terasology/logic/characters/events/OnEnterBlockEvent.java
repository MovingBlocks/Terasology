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
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

/**
 */
public class OnEnterBlockEvent implements Event {
    private Block oldBlock;
    private Block newBlock;
    private Vector3i characterRelativePosition;

    public OnEnterBlockEvent(Block oldBlock, Block newBlock, Vector3i characterRelativePosition) {
        this.oldBlock = oldBlock;
        this.newBlock = newBlock;
        this.characterRelativePosition = characterRelativePosition;
    }

    public Block getNewBlock() {
        return newBlock;
    }

    public Block getOldBlock() {
        return oldBlock;
    }

    public Vector3i getCharacterRelativePosition() {
        return characterRelativePosition;
    }
}

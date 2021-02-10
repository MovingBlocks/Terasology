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
package org.terasology.logic.characters.interactions;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;

/**
 * Represents the end of an interaction between for example a character and a container.
 * <br><br>
 * The event is sent via the character.
 * <br><br>
 * The event is sent by the server to the owner of the character..
 *
 */
@OwnerEvent
public class InteractionEndEvent implements Event {
    private int interactionId;

    protected InteractionEndEvent() {
    }

    public InteractionEndEvent(int interactionId) {
        this.interactionId = interactionId;
    }

    public int getInteractionId() {
        return interactionId;
    }
}

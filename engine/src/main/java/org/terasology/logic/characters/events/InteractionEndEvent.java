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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;
import org.terasology.logic.characters.CharacterUtil;
/**
 * Represents the start of an interaction between for example a character and a container.
 *
 * Don't send this event manually. Instead use {@link CharacterUtil#setInteractionTarget(EntityRef, EntityRef)} to do
 * so.
 *
 * When event handler with priority low or higher runs, the  interactionTarget field of the instigator's
 * CharacterComponent will still have the old value.
 *
 * @author Florian <florian@fkoeberle.de>
 */
@BroadcastEvent
public class InteractionEndEvent implements Event {
    private EntityRef instigator;

    protected InteractionEndEvent() {
    }

    public InteractionEndEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    /**
     * @return the character which started the interaction.
     */
    public EntityRef getInstigator() {
        return instigator;
    }
}

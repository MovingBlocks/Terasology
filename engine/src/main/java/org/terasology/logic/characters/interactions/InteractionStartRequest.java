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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;
import org.terasology.logic.characters.interactions.InteractionUtil;

/**
 *
 * Requests the server to announce the start of an interaction between for example a character and a container.
 *
 * Don't send this event manually. Instead use {@link InteractionUtil#setInteractionTarget(EntityRef, EntityRef)} to do
 * so.
 *
 * The event is sent to the investigator as the own character is one of the few entity a client is
 * allowed to send events to.
 * *
 * The server sets the interactionTarget field of the character (instigator = character entity).
 *
 * @author Florian <florian@fkoeberle.de>
 */
@ServerEvent
public class InteractionStartRequest implements Event {
    private EntityRef target;

    protected InteractionStartRequest() {
    }

    public InteractionStartRequest(EntityRef target) {
        this.target = target;
    }

    /**
     * @return the entity with which the character started interacting with.
     */
    public EntityRef getTarget() {
        return target;
    }
}

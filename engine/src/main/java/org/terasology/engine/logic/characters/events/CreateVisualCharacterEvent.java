/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.logic.characters.VisualCharacterComponent;

/**
 * Sent to the character entities when a visual representation of it should be created for them: The event will be send
 * to characters that have the {@link VisualCharacterComponent}.
 *
 * When you want to create a new type of visual character just create a handler for this event and consume it.
 * The handler should create the visual representation of the character via the builder provided by the even.t
 *
 * There is a default handling on {@link EventPriority#PRIORITY_TRIVIAL}. The default handler creates
 * a placeholder character. To prevent this placeholder character to be created the event must be consumed
 * by a handler that creates its own visual character.
 *
 * Typically gets only sent to characters of other players
 * and not to the owned character as in first person you don't see yourself.
 */
public class CreateVisualCharacterEvent extends AbstractConsumableEvent {
    private EntityBuilder visualCharacterBuilder;

    public CreateVisualCharacterEvent(EntityBuilder visualCharacterBuilder) {
        this.visualCharacterBuilder = visualCharacterBuilder;
    }

    public EntityBuilder getVisualCharacterBuilder() {
        return visualCharacterBuilder;
    }
}

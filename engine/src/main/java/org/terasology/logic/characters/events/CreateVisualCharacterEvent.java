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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.logic.location.Location;
import org.terasology.logic.characters.VisualCharacterComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

/**
 * Send to the character entities when a visual represenation of it should be created for them: The event will be send
 * to characters that have the {@link VisualCharacterComponent}.
 *
 * When you want to create a new type of visual character just create a handler for this event and consume it.
 * The handler should create the visual represenation of the character.
 *
 * Typically it is a client system (as headless server needs no visuals) and attaches the visual character to
 * the client one via {@link Location#attachChild(EntityRef, EntityRef, Vector3f, Quat4f)}.
 * The event handler should be coupled on a component to that also n
 *
 * There is a default handling on {@link EventPriority#PRIORITY_TRIVIAL} that creates a placeholder.
 * Typically gets only sent to other players
 */
public class CreateVisualCharacterEvent extends AbstractConsumableEvent {
}

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
package org.terasology.logic.characters;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.events.CreateVisualCharacterEvent;
import org.terasology.network.NoReplicate;
import org.terasology.network.Replicate;

/**
 * Add this component to characters to give them a visual appearance.
 *
 * This component does not specify however how the character look like.
 * Instead event handlers can be implemented for a {@link CreateVisualCharacterEvent}
 * that create a visual character entity. The event gets automatically send,
 * when a visual character needs to be created.
 *
 *
 */
@Replicate
public class VisualCharacterComponent implements Component {
    /**
     * Should not be set manually. Can however be used to forward events from character to visual character.
     */
    @NoReplicate
    public EntityRef visualCharacter = EntityRef.NULL;
}

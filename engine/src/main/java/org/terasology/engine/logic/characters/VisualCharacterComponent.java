// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.events.CreateVisualCharacterEvent;
import org.terasology.engine.network.NoReplicate;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

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
public class VisualCharacterComponent implements Component<VisualCharacterComponent> {
    /**
     * Should not be set manually. Can however be used to forward events from character to visual character.
     */
    @NoReplicate
    public EntityRef visualCharacter = EntityRef.NULL;

    @Override
    public void copyFrom(VisualCharacterComponent other) {
        this.visualCharacter = other.visualCharacter;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

/**
 *
 * Sent to the client by itself at the end of an interaction between a character and a target.
 *
 * THe event is sent to the target entity.
 *
 * This event should not be sent manually by modules: Modules that want to end an interaction should use the utility
 * class {@link InteractionUtil} to do so.
 *
 * When event handler runs, the  predictedInteractionTarget field of the instigator's
 * CharacterComponent will already be updated to the new value.
 *
 */
@API
public class InteractionEndPredicted implements Event {
    private EntityRef instigator;

    protected InteractionEndPredicted() {
    }

    public InteractionEndPredicted(EntityRef instigator) {
        this.instigator = instigator;
    }

    /**
     * @return the character which stopped the interaction.
     */
    public EntityRef getInstigator() {
        return instigator;
    }
}

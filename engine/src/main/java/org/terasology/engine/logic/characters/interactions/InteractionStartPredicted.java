// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

/**
 * Sent to the client by itself at the start of an interaction between a character and a target.
 * <br><br>
 * THe event is sent to the target entity.
 * <br><br>
 * This event is not intended to be sent by modules.
 * <br><br>
 * When event handler runs, the  predictedInteractionTarget field of the instigator's
 * CharacterComponent will already be updated to the new value.
 *
 */
@API
public class InteractionStartPredicted implements Event {
    private EntityRef instigator;

    protected InteractionStartPredicted() {
    }

    public InteractionStartPredicted(EntityRef instigator) {
        this.instigator = instigator;
    }

    /**
     * @return he character which started the interaction.
     */
    public EntityRef getInstigator() {
        return instigator;
    }
}

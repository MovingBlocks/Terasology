// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

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

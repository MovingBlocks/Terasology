// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.Event;

/**
 * This event is attached to the character.
 *
 * It is sent when a {@link ActivationRequest} was not acceptable by the server.
 *
 * Modules are allowed to process the event, but should not sent it.
 *
 * This is an authority internal event. If a reaction should be sent the client is up to the systems that process this
 * event..
 *
 */
public class ActivationRequestDenied implements Event {
    private int activationId;

    protected ActivationRequestDenied() {
    }

    public ActivationRequestDenied(int activationId) {
        this.activationId = activationId;
    }

    public int getActivationId() {
        return activationId;
    }
}

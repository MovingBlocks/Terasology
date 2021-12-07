// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Request the server to cancel the current interaction.
 *
 */
@ServerEvent
public class InteractionEndRequest implements Event {

    public InteractionEndRequest() {
    }

}


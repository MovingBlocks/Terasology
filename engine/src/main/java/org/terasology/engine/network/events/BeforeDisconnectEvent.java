// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.events;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Event notifying of a client scheduled for disconnect.
 */
public class BeforeDisconnectEvent implements Event {

    public BeforeDisconnectEvent() {
    }
}

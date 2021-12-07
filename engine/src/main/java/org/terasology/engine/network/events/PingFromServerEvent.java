// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.events;

import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event handles the ping from the server to all clients.
 */
@OwnerEvent
public class PingFromServerEvent implements Event {
}

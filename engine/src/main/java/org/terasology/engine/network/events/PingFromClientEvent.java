// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.events;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event handles the ping from one client to the server.
 */
@ServerEvent
public class PingFromClientEvent implements Event {
}

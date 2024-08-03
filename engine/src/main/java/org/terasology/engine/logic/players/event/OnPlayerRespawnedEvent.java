// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players.event;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 *  This event gets sent when the player respawns.<br>
 *  The player entity is preserved along with its components during respawn.<br>
 *  This event should be received and handled by systems that need to reset
 *  some components attached to the player entity.
 */
public class OnPlayerRespawnedEvent implements Event {
}

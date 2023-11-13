// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players.event;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event gets sent when the player spawns.
 * <br>
 * <b>Note:</b> that this should be used only as a one time event i.e. when
 * the player spawns for the first time in the game.
 * On every subsequent spawn a onPlayerRespawnedEvent is sent.
 */
public class OnPlayerSpawnedEvent implements Event {
}

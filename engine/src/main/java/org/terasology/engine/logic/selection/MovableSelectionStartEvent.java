// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.module.sandbox.API;

/**
 * This event should be sent by a system after it receives a {@link ApplyBlockSelectionEvent} which marks the end of a
 * region selection. This event marks the start of the binding of the camera position with the selected region.
 * <br>
 * The entity used to send this event must have the {@link org.terasology.world.selection.BlockSelectionComponent}
 */
@API
public class MovableSelectionStartEvent implements Event {
}

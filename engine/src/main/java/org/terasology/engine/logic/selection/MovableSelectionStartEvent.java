// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.terasology.engine.world.selection.BlockSelectionComponent;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

/**
 * This event should be sent by a system after it receives a {@link ApplyBlockSelectionEvent} which marks the end of a
 * region selection. This event marks the start of the binding of the camera position with the selected region.
 * <br>
 * The entity used to send this event must have the {@link BlockSelectionComponent}
 */
@API
public class MovableSelectionStartEvent implements Event {
}

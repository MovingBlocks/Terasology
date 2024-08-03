// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players.event;

import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event gets sent when the {@link LocalPlayer} object is ready to be used.
 * <br>
 * The object can be injected using {@link org.terasology.engine.registry.In}.
 * This event corresponds with its isValid() method returning true for the first time.
 */
public class LocalPlayerInitializedEvent implements Event {
}

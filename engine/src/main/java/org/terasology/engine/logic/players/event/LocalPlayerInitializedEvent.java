// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players.event;

import org.terasology.engine.entitySystem.event.Event;

/**
 * This event gets sent when the {@link org.terasology.logic.players.LocalPlayer} object is ready to be used. <br/> The
 * object can be injected using {@link org.terasology.registry.In}. This event corresponds with its isValid() method
 * returning true for the first time.
 */
public class LocalPlayerInitializedEvent implements Event {
}

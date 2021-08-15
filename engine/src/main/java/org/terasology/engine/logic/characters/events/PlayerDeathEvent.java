// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 * This event is sent to the player entity when the player dies.
 */
@OwnerEvent
public class PlayerDeathEvent implements Event {
    public String damageTypeName;
    public String instigatorName;
}

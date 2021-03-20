// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 * This event is sent to the client entity when the character dies.
 */
@OwnerEvent
public class DeathEvent implements Event {
    public String damageTypeName;
    public String instigatorName;
}

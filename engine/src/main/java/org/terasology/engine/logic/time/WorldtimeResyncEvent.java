// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.time;

import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@OwnerEvent
public class WorldtimeResyncEvent implements Event {
    public float days;

    public WorldtimeResyncEvent() {
    }

    public WorldtimeResyncEvent(float days) {
        this.days = days;
    }
}

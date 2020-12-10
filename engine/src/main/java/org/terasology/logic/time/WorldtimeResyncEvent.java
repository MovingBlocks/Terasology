// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.time;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;

@OwnerEvent
public class WorldtimeResyncEvent implements Event {
    public float days;

    public WorldtimeResyncEvent() {
    }

    public WorldtimeResyncEvent(float days) {
        this.days = days;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.time;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

@OwnerEvent
public class WorldtimeResyncEvent implements Event {
    public float days;

    public WorldtimeResyncEvent() {
    }

    public WorldtimeResyncEvent(float days) {
        this.days = days;
    }
}

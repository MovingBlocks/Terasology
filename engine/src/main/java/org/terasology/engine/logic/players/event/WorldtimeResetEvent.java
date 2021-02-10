// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.players.event;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

@ServerEvent
public class WorldtimeResetEvent implements Event {

    public float days;

    public WorldtimeResetEvent() {
    }

    public WorldtimeResetEvent(float days) {
        this.days = days;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players.event;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@ServerEvent
public class WorldtimeResetEvent implements Event {

    public float days;

    public WorldtimeResetEvent() {
    }

    public WorldtimeResetEvent(float days) {
        this.days = days;
    }
}

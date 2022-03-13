// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.time;

import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@BroadcastEvent
public class TimeResynchEvent implements Event {
    private float gameTimeDilation;

    public TimeResynchEvent() {
    }

    public TimeResynchEvent(float gameTimeDilation) {
        this.gameTimeDilation = gameTimeDilation;
    }

    public float getGameTimeDilation() {
        return gameTimeDilation;
    }
}

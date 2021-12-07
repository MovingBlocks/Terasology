// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import org.terasology.gestalt.entitysystem.event.Event;

public class DelayedActionTriggeredEvent implements Event {
    private String actionId;

    public DelayedActionTriggeredEvent(String actionId) {
        this.actionId = actionId;
    }

    public String getActionId() {
        return actionId;
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import org.terasology.engine.entitySystem.event.Event;

/**
 * @deprecated Use DelayManager::cancelDelayedAction instead.
 */
@Deprecated
public class CancelDelayedActionEvent implements Event {
    private final String actionId;

    public CancelDelayedActionEvent(String actionId) {
        this.actionId = actionId;
    }

    public String getActionId() {
        return actionId;
    }
}

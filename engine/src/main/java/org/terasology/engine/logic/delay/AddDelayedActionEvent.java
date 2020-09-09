// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import org.terasology.engine.entitySystem.event.Event;

/**
 * @deprecated Use DelayManager::addDelayedAction instead.
 */
@Deprecated
public class AddDelayedActionEvent implements Event {
    private final String actionId;
    private final long delay;

    public AddDelayedActionEvent(String actionId, long delay) {
        this.actionId = actionId;
        this.delay = delay;
    }

    public String getActionId() {
        return actionId;
    }

    public long getDelay() {
        return delay;
    }
}

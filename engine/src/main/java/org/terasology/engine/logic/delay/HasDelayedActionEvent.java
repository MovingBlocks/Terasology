// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import org.terasology.engine.entitySystem.event.Event;

/**
 * @deprecated Use DelayManager::hasDelayedAction instead.
 */
@Deprecated
public class HasDelayedActionEvent implements Event {
    private final String actionId;
    private boolean result;

    public HasDelayedActionEvent(String actionId) {
        this.actionId = actionId;
    }

    public String getActionId() {
        return actionId;
    }

    public boolean hasAction() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}

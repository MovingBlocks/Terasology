// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * The state of a behavior node.
 */
public enum BehaviorState {
    UNDEFINED(false),
    FAILURE(true),
    SUCCESS(true),
    RUNNING(false);

    private boolean finished;

    BehaviorState(boolean finished) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return finished;
    }
}

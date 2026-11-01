// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.context.annotation.API;
import org.terasology.nui.properties.Range;

/**
 * Runs the child for a given time.
 * Returns when the child returns,
 * or returns FAILURE if child doesn't finish in time.
 */
@API
@BehaviorAction(name = "timeout", isDecorator = true)
public class TimeoutAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutAction.class);

    @Range(min = 0, max = 10)
    private float time;

    @Override
    public void construct(Actor actor) {
        actor.setValue(getId(), time);
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        switch (result) {
            case UNDEFINED:
                // If used with no child specified
                logger.error("TimeoutAction received an UNDEFINED state to modify. Is a child specified?");
                float timeRemaining = actor.getValue(getId());
                timeRemaining -= actor.getDelta();
                actor.setValue(getId(), timeRemaining);
                return timeRemaining > 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
            case RUNNING:
                // If child is still running
                float timeRemaining2 = actor.getValue(getId());
                timeRemaining2 -= actor.getDelta();
                actor.setValue(getId(), timeRemaining2);
                return timeRemaining2 > 0 ? BehaviorState.RUNNING : BehaviorState.FAILURE;
            default:
                // If child returned a final state, pass the state on
                return result;
        }

    }
}

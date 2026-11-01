// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.context.annotation.API;
import org.terasology.nui.properties.Range;

/**
 * Sleeps for a given amount of time (RUNNING),
 * then returns with SUCCESS.
 */
@API
@BehaviorAction(name = "sleep")
public class SleepAction extends BaseAction {
    @Range(min = 0, max = 20)
    private float time;

    @Override
    public void construct(Actor actor) {
        actor.setValue(getId(), time);
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {

        float timeRemaining = 0;
        try { // TODO figure out the delegation issue
            timeRemaining = actor.getValue(getId());
        } catch (NullPointerException e) {
            construct(actor);
        }
        timeRemaining -= actor.getDelta();
        actor.setValue(getId(), timeRemaining);
        return timeRemaining > 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
    }

}

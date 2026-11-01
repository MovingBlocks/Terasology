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
 * Runs child for the given amount of time.
 * Can be used without specifying a child as a simple 'wait' action.
 */
@API
@BehaviorAction(name = "counter", isDecorator = true)
public class CounterAction extends BaseAction {
    @Range(min = 0, max = 100)
    private int count;

    @Override
    public void construct(Actor actor) {
        actor.setValue(getId(), count);
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (result == BehaviorState.SUCCESS) {
            int remaining = actor.getValue(getId());
            remaining--;
            actor.setValue(getId(), remaining);
            return remaining > 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
        }
        return result;
    }
}

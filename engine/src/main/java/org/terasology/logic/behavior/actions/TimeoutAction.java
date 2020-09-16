/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.module.sandbox.API;
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

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

import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.module.sandbox.API;
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

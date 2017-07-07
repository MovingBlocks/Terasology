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
package org.terasology.logic.behavior.actions.conditions;

import org.terasology.entitySystem.Component;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;

/**
 *
 */
@BehaviorAction(name = "condition")
public class ConditionAction extends BaseAction {
    private String componentPresent;
    private String componentAbsent;


    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {

        try {
            if (!condition(actor)) {
                return BehaviorState.FAILURE;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return BehaviorState.SUCCESS;
    }

    private boolean condition(Actor actor) throws ClassNotFoundException {
        boolean passing = true;
        if (componentAbsent != null) {
            if (actor.hasComponent((Class<? extends Component>) Class.forName(componentPresent))) {
                passing = false;
            }
        }

        if (componentPresent != null) {
            if (!actor.hasComponent((Class<? extends Component>) Class.forName(componentPresent))) {
                passing = false;
            }
        }

        return passing;
    }


}

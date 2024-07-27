// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.context.annotation.API;

/**
 * Inverts the child's return value. Doesn't change RUNNING.
 */
@API
@BehaviorAction(name = "invert", isDecorator = true)
public class InvertAction extends BaseAction {
    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        switch (state) {
            case FAILURE:
                return BehaviorState.SUCCESS;
            case RUNNING:
                return BehaviorState.RUNNING;
            case SUCCESS:
                return BehaviorState.FAILURE;
            default:
                return BehaviorState.UNDEFINED;

        }
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.context.annotation.API;

/**
 * Node, that loops its child forever
 */
@API
@BehaviorAction(name = "loop", isDecorator = true)
public class LoopAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        return BehaviorState.RUNNING;
    }
}

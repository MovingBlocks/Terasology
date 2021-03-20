// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;

/*
  This is a test-related action.
  TODO move this to engine-tests once a cleaner Gestalt option is available
 */
@BehaviorAction(name = "print")
public class Print extends BaseAction {
    public static StringBuilder output = new StringBuilder();

    private String msg;

    @Override
    public void construct(Actor actor) {
        output.append("[");
    }

    @Override
    public void destruct(Actor actor) {
        output.append("]");
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        output.append(msg);
        return BehaviorState.SUCCESS;
    }
}

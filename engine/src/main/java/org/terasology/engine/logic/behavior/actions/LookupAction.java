// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.context.annotation.API;
import org.terasology.nui.properties.OneOf;

/**
 * Runs a given behavior tree.
 */
@API
@BehaviorAction(name = "lookup")
public class LookupAction extends BaseAction {
    @OneOf.Provider(name = "behaviorTrees")
    private BehaviorTree tree;

    @Override
    public void construct(Actor actor) {
        if (tree != null) {
            BehaviorNode root = tree.getRoot().deepCopy();
            if (root != null) {
                actor.setValue(getId(), root);
                root.construct(actor);
            }
        }
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        BehaviorNode root = actor.getValue(getId());
        if (root == null) {
            return BehaviorState.FAILURE;
        }
        return root.execute(actor);
    }

    @Override
    public void destruct(Actor actor) {
        BehaviorNode root = actor.getValue(getId());
        if (root != null) {
            root.destruct(actor);
        }
    }
}

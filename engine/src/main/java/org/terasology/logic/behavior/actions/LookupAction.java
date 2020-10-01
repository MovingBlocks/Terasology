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
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.module.sandbox.API;
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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.behavior.core.CollectiveBehaviorTreeRunner;
import org.terasology.engine.logic.behavior.core.DelegateNode;

import java.util.Set;

/**
 * Tree runner, that runs the given tree for a group of actors.
 */
public class DefaultCollectiveBehaviorTreeRunner implements CollectiveBehaviorTreeRunner {
    private final BehaviorTree tree;
    private final BehaviorNode root;
    private Callback callback;
    private Set<Actor> actors;
    private BehaviorState state = BehaviorState.UNDEFINED;

    public DefaultCollectiveBehaviorTreeRunner(BehaviorNode node, Set<Actor> actors) {
        this.tree = null;
        this.root = node.deepCopy();
        this.actors = actors;
    }

    public DefaultCollectiveBehaviorTreeRunner(BehaviorTree tree, Set<Actor> actors, Callback callback) {
        this.callback = callback;
        this.tree = tree;
        this.root = injectDelegates(tree.getRoot().deepCopy(), tree.getRoot());

        this.actors = actors;
    }

    private BehaviorNode injectDelegates(BehaviorNode newNode, BehaviorNode treeNode) {
        if (newNode.getChildrenCount() == 0) {
            return createCallbackNode(newNode, treeNode);
        } else {
            for (int i = 0; i < newNode.getChildrenCount(); i++) {
                newNode.replaceChild(i, injectDelegates(newNode.getChild(i), treeNode.getChild(i)));
            }
            return createCallbackNode(newNode, treeNode);
        }
    }

    private DelegateNode createCallbackNode(BehaviorNode newNode, final BehaviorNode treeNode) {
        return new DelegateNode(newNode) {
            @Override
            public BehaviorState execute(Actor theActor) {
                BehaviorState result = super.execute(theActor);
                if (callback != null) {
                    callback.afterExecute(treeNode, result);
                }
                return result;
            }
        };
    }

    @Override
    public BehaviorTree getTree() {
        return tree;
    }

    @Override
    public BehaviorState step() {
        for(Actor actor: this.actors) {
            if (state != BehaviorState.RUNNING) {
                root.construct(actor);
            }

            state = root.execute(actor);
            if (state != BehaviorState.RUNNING) {
                root.destruct(actor);
            }
        }

        return state;
    }

    @Override
    public Set<Actor> getActors() {
        return actors;
    }

    @Override
    public void setActors(Set<Actor> actors) {
        this.actors = actors;
    }
}

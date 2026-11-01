// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.CollectiveBehaviorTreeRunner;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.context.annotation.API;

import java.util.Set;

/**
 * Evaluates a behavior tree for a group of actors.
 * This is necessary for synchronized behavior changes on each step.
 * Uses BehaviorTreeRunner to actually evaluate the tree. The runner
 * is kept up to date when there are modifications at the tree. *
 */
@API
public class CollectiveInterpreter {
    private Set<Actor> actors;
    private CollectiveBehaviorTreeRunner treeRunner;
    private BehaviorTree tree;
    private Callback callback;

    public CollectiveInterpreter(Set<Actor> actors) {
        this.actors = actors;
    }

    /**
     * Copy constructor to save BT execution state
     * @param collectiveInterpreter
     */
    public CollectiveInterpreter(CollectiveInterpreter collectiveInterpreter) {
        this.actors = collectiveInterpreter.actors;
        this.treeRunner = collectiveInterpreter.treeRunner;
        this.tree = collectiveInterpreter.tree;
        this.callback = collectiveInterpreter.callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
        reset();
    }

    public Set<Actor> actors() {
        return actors;
    }

    public void reset() {
        treeRunner = null;
    }

    public void tick(float delta) {
        for (Actor actor : this.actors) {
            actor.setDelta(delta);
        }
        if (treeRunner == null && tree != null) {
            treeRunner = new DefaultCollectiveBehaviorTreeRunner(tree, actors, callback);
        }
        if (treeRunner != null) {
            treeRunner.step();
        }
    }

    public void run() {
    }

    public void setTree(BehaviorTree tree) {
        this.tree = tree;
        reset();
    }

    public BehaviorTree getTree() {
        return tree;
    }

    @Override
    public String toString() {
        String returnString = "";
        for (Actor actor : this.actors) {
            if (actor.hasComponent(DisplayNameComponent.class)) {
                returnString = returnString + actor.getComponent(DisplayNameComponent.class).name
                        + System.lineSeparator();
            }
            returnString = returnString + "unnamed " + actor.getEntity().getParentPrefab().getName()
                    + System.lineSeparator();
        }
        return returnString;
    }

    public void pause() {

    }
}

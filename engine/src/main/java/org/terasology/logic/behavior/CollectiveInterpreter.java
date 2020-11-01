/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.behavior;

import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.CollectiveBehaviorTreeRunner;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.module.sandbox.API;

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
        for(Actor actor : this.actors) {
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
        for(Actor actor : this.actors) {
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

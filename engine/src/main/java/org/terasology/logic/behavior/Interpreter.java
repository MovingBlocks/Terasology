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
package org.terasology.logic.behavior;

import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorTreeRunner;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.module.sandbox.API;

/**
 * An interpreter evaluates a behavior tree. Uses BehaviorTreeRunner to actually evaluate the tree. The runner
 * is kept up to date when there are modifications at the tree.
 *
 */
@API
public class Interpreter {

    private Actor actor;
    private BehaviorTreeRunner treeRunner;
    private BehaviorTree tree;
    private DefaultBehaviorTreeRunner.Callback callback;

    public Interpreter(Actor actor) {
        this.actor = actor;
    }

    /**
     * Copy constructor to save BT execution state
     * @param interpreter
     */
    public Interpreter(Interpreter interpreter) {
        this.actor = interpreter.actor;
        this.treeRunner = interpreter.treeRunner;
        this.tree = interpreter.tree;
        this.callback = interpreter.callback;
    }

    public void setCallback(DefaultBehaviorTreeRunner.Callback callback) {
        this.callback = callback;
        reset();
    }

    public Actor actor() {
        return actor;
    }

    public void reset() {
        treeRunner = null;
    }

    public void tick(float delta) {
        actor.setDelta(delta);
        if (treeRunner == null && tree != null) {
            treeRunner = new DefaultBehaviorTreeRunner(tree, actor, callback);
            //        Assembler assembler = new Assembler("Test", tree.getRoot());
            //        treeRunner = assembler.createInstance(actor);
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
        if (actor.hasComponent(DisplayNameComponent.class)) {
            return actor.getComponent(DisplayNameComponent.class).name;
        }

        if (actor.getEntity() == null || actor.getEntity().getParentPrefab() == null) {
            return "no entity";
        }

        return "unnamed " + actor.getEntity().getParentPrefab().getName();
    }

    public void pause() {

    }
}

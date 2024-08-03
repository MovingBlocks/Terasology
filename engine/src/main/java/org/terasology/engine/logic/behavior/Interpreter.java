// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BehaviorTreeRunner;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.context.annotation.API;

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

/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.behavior.core;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

import java.util.Iterator;

/**
 * Runs all children until one finishes with FAILURE
 */
public class SequenceNode extends CompositeNode {
    private Iterator<BehaviorNode> iterator;
    private BehaviorNode current;
    private String reentry;

    @Override
    public String getName() {
        return "sequence";
    }

    @Override
    public BehaviorNode deepCopy() {
        SequenceNode result = new SequenceNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        iterator = children.iterator();
        nextChild(actor);
    }

    @Override
    public BehaviorState execute(Actor actor) {
        BehaviorState result;
        while (current != null) {
            result = current.execute(actor);
            if (result == BehaviorState.RUNNING) {
                return BehaviorState.RUNNING;
            }
            current.destruct(actor);
            if (result == BehaviorState.FAILURE) {
                return BehaviorState.FAILURE;
            } else {
                nextChild(actor);
            }
        }
        return BehaviorState.SUCCESS;
    }

    private void nextChild(Actor actor) {
        if (iterator.hasNext()) {
            current = iterator.next();
            current.construct(actor);
        } else {
            current = null;
        }
    }

    @Override
    public void destruct(Actor actor) {
    }

    @Override
    public void assembleSetup(ClassGenerator gen) {
        super.assembleSetup(gen);
        reentry = gen.generateField(Type.INT_TYPE);
    }

    @Override
    public void assembleConstruct(MethodGenerator gen) {
        gen.loadThis();
        gen.push(-1);
        gen.storeField(reentry);
    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        Label[] labels = new Label[children.size()];
        for (int i = 0; i < children.size(); i++) {
            labels[i] = gen.newLabel();
        }
        Label defaultLabel = gen.newLabel();
        Label exitSuccess = gen.newLabel();
        Label exitFailure = gen.newLabel();
        Label exitRunning = gen.newLabel();

        gen.loadThis();
        gen.loadField(reentry);
        gen.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);

        Label loop = gen.mark();
        for (int i = 0; i < labels.length; i++) {
            BehaviorNode child = children.get(i);
            child.assembleConstruct(gen);
            gen.mark(labels[i]);

            gen.loadThis();
            gen.push(i);
            gen.storeField(reentry);

            child.assembleExecute(gen);
            gen.dup();
            gen.push(BehaviorState.RUNNING.ordinal());
            gen.ifICmp(GeneratorAdapter.EQ, exitRunning);

            child.assembleDestruct(gen);
            gen.push(BehaviorState.SUCCESS.ordinal());
            gen.ifICmp(GeneratorAdapter.NE, exitFailure);
        }

        gen.push(BehaviorState.SUCCESS.ordinal());
        gen.goTo(exitSuccess);

        gen.mark(defaultLabel);
        gen.goTo(loop);

        gen.mark(exitFailure);
        gen.push(BehaviorState.FAILURE.ordinal());

        gen.mark(exitSuccess);

        gen.loadThis();
        gen.push(-1);
        gen.storeField(reentry);

        gen.mark(exitRunning);
    }
}

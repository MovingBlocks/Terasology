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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

import static org.objectweb.asm.commons.GeneratorAdapter.EQ;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;

/**
 * Runs all children parallel.
 */
public class ParallelNode extends CompositeNode {
    @Override
    public String getName() {
        return "parallel";
    }

    @Override
    public BehaviorNode deepCopy() {
        ParallelNode result = new ParallelNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        for (BehaviorNode child : children) {
            child.construct(actor);
        }
    }

    @Override
    public BehaviorState execute(Actor actor) {
        int successCounter = 0;
        for (BehaviorNode child : children) {
            BehaviorState result = child.execute(actor);
            if (result == BehaviorState.FAILURE) {
                return BehaviorState.FAILURE;
            }
            if (result == BehaviorState.SUCCESS) {
                successCounter++;
            }
        }
        return successCounter == children.size() ? BehaviorState.SUCCESS : BehaviorState.RUNNING;
    }

    @Override
    public void destruct(Actor actor) {
        for (BehaviorNode child : children) {
            child.destruct(actor);
        }
    }

    @Override
    public void assembleConstruct(MethodGenerator gen) {
        for (BehaviorNode child : children) {
            child.assembleConstruct(gen);
        }
    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        Label exit = gen.newLabel();

        int successCounter = gen.newLocal(Type.INT_TYPE);
        gen.push(0);
        gen.storeLocal(successCounter);
        for (BehaviorNode child : children) {
            child.assembleExecute(gen);

            gen.dup();
            gen.push(BehaviorState.FAILURE.ordinal());
            gen.ifICmp(EQ, exit);

            Label skip = gen.newLabel();
            gen.push(BehaviorState.SUCCESS.ordinal());
            gen.ifICmp(NE, skip);

            gen.loadLocal(successCounter);
            gen.push(1);
            gen.visitInsn(Opcodes.IADD);
            gen.storeLocal(successCounter);

            gen.mark(skip);
        }
        gen.push(BehaviorState.RUNNING.ordinal());

        gen.loadLocal(successCounter);
        gen.push(children.size());
        gen.ifICmp(NE, exit);

        gen.pop();
        gen.push(BehaviorState.SUCCESS.ordinal());

        gen.mark(exit);
    }

    @Override
    public void assembleDestruct(MethodGenerator gen) {
        for (BehaviorNode child : children) {
            child.assembleDestruct(gen);
        }
    }

}

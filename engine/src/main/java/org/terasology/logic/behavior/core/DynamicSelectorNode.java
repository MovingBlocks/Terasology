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
import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

import java.util.BitSet;

import static org.objectweb.asm.commons.GeneratorAdapter.EQ;

/**
 * Works like a normal selector but each update tick, all children are checked for state changes.
 */
public class DynamicSelectorNode extends CompositeNode {
    private BitSet constructed;
    private String[] byteFields;

    @Override
    public String getName() {
        return "dynamic";
    }

    @Override
    public BehaviorNode deepCopy() {
        DynamicSelectorNode result = new DynamicSelectorNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        constructed = new BitSet(children.size());
    }

    @Override
    public BehaviorState execute(Actor actor) {
        BehaviorState result;
        for (int i = 0; i < children.size(); i++) {
            BehaviorNode child = children.get(i);
            if (!constructed.get(i)) {
                child.construct(actor);
                constructed.set(i);
            }
            result = child.execute(actor);
            if (result == BehaviorState.RUNNING) {
                return BehaviorState.RUNNING;
            }
            child.destruct(actor);
            constructed.clear(i);
            if (result == BehaviorState.SUCCESS) {
                return BehaviorState.SUCCESS;
            }
        }
        return BehaviorState.FAILURE;
    }

    @Override
    public void destruct(Actor actor) {
    }

    @Override
    public void assembleSetup(ClassGenerator gen) {
        super.assembleSetup(gen);

        int bytes = children.size() / 8;
        if (children.size() % 8 > 0) {
            bytes++;
        }
        byteFields = new String[bytes];
        for (int i = 0; i < bytes; i++) {
            byteFields[i] = gen.generateField(Type.BYTE_TYPE);
        }
    }

    private void loadBitCmp(MethodGenerator gen, int index) {
        int b = index / 8;
        int bit = index % 8;

        gen.loadThis();
        gen.loadField(byteFields[b]);
        gen.push(1 << bit);
        gen.visitInsn(Opcodes.IAND);
        gen.push(1 << bit);
    }

    private void storeBit(MethodGenerator gen, int index) {
        int b = index / 8;
        int bit = index % 8;

        gen.loadThis();
        gen.loadThis();
        gen.loadField(byteFields[b]);
        gen.push(1 << bit);
        gen.visitInsn(Opcodes.IOR);
        gen.storeField(byteFields[b]);
    }

    private void resetBit(MethodGenerator gen, int index) {
        int b = index / 8;
        int bit = index % 8;

        gen.loadThis();
        gen.loadThis();
        gen.loadField(byteFields[b]);
        gen.push(0xff & (~(1 << bit)));
        gen.visitInsn(Opcodes.IAND);
        gen.storeField(byteFields[b]);
    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        Label exit = gen.newLabel();
        Label exitSuccess = gen.newLabel();

        for (int i = 0; i < children.size(); i++) {
            Label skip = gen.newLabel();
            BehaviorNode child = children.get(i);

            loadBitCmp(gen, i);
            gen.ifICmp(EQ, skip);
            child.assembleConstruct(gen);
            storeBit(gen, i);

            gen.mark(skip);
            child.assembleExecute(gen);

            gen.dup();
            gen.push(BehaviorState.RUNNING.ordinal());
            gen.ifICmp(EQ, exit);

            child.assembleDestruct(gen);
            resetBit(gen, i);

            gen.push(BehaviorState.SUCCESS.ordinal());
            gen.ifICmp(EQ, exitSuccess);
        }

        gen.push(BehaviorState.FAILURE.ordinal());
        gen.goTo(exit);

        gen.mark(exitSuccess);
        gen.push(BehaviorState.SUCCESS.ordinal());

        gen.mark(exit);
    }
}

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
package org.terasology.logic.behavior.core.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.terasology.logic.behavior.core.Action;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorState;

/**
 * Helper class to generate java methods using asm.
 */
public class MethodGenerator extends GeneratorAdapter {
    private ClassGenerator classGen;

    public MethodGenerator(ClassGenerator classGen, int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.classGen = classGen;
    }

    public void loadField(String name) {
        getField(classGen.getType(), name, classGen.getFieldType(name));
    }

    public void storeField(String name) {
        putField(classGen.getType(), name, classGen.getFieldType(name));
    }

    public void invokeAction(int id, String method) {
        invokeAction(id, method, null);
    }

    public void invokeAction(int id, String method, BehaviorState state) {
        Method m = Method.getMethod("org.terasology.logic.behavior.core.Action getAction(int)");
        Method callback = Method.getMethod(method);
        loadThis();
        push(id);
        invokeVirtual(classGen.getType(), m);

        loadThis();
        getField(Type.getType(CompiledBehaviorTree.class), "actor", Type.getType(Actor.class));

        if (state != null) {
            Method values = Method.getMethod("org.terasology.logic.behavior.core.BehaviorState[] values()");
            invokeStatic(Type.getType(BehaviorState.class), values);
            push(state.ordinal());
            arrayLoad(Type.getType(BehaviorState.class));
        }

        invokeInterface(Type.getType(Action.class), callback);

        if (state != null) {
            Method ordinal = Method.getMethod("int ordinal()");
            invokeVirtual(Type.getType(BehaviorState.class), ordinal);
        }
    }
}

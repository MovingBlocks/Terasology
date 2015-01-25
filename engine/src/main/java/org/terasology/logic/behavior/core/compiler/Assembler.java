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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.AssemblingBehaviorNode;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorState;

/**
 * Converts a behavior tree to bytecode using code generation with asm. The byte code can be stored to disk and
 * operate like any normal java class file - or you can create instances directly using its own class loader.
 */
public class Assembler {

    private final String className;
    private final ClassGenerator generator;
    private final ClassWriter classWriter;
    private MyClassLoader loader;
    private Class type;
    private BehaviorNode node;

    public Assembler(String className, BehaviorNode node) {
        this.className = className;
        this.node = node;
        classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        generator = new ClassGenerator(classWriter, className);

        generator.generateMethod("void <init>()", new Consumer<MethodGenerator>() {
            @Override
            public void accept(MethodGenerator gen) {
                gen.loadThis();
                gen.invokeConstructor(Type.getType(CompiledBehaviorTree.class), Method.getMethod("void <init>()"));
                gen.returnValue();
            }
        });

        generateMethod(node);
    }

    public byte[] getBytecode() {
        return classWriter.toByteArray();
    }

    public CompiledBehaviorTree createInstance(Actor actor) {
        if (loader == null) {
            loader = new MyClassLoader(this.getClass().getClassLoader());
            type = loader.defineClass(className, getBytecode());
        }
        try {
            CompiledBehaviorTree tree = (CompiledBehaviorTree) type.newInstance();
            tree.bind(node);
            tree.setActor(actor);
            return tree;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected void generateMethod(final AssemblingBehaviorNode aNode) {
        aNode.assembleSetup(generator);
        generator.generateMethod("int run(int)", new Consumer<MethodGenerator>() {
            @Override
            public void accept(MethodGenerator gen) {
                gen.loadArg(0);
                gen.push(BehaviorState.RUNNING.ordinal());
                Label skip = gen.newLabel();
                gen.ifICmp(GeneratorAdapter.EQ, skip);
                aNode.assembleConstruct(gen);
                gen.mark(skip);

                aNode.assembleExecute(gen);

                gen.dup();
                gen.push(BehaviorState.RUNNING.ordinal());
                skip = gen.newLabel();
                gen.ifICmp(GeneratorAdapter.EQ, skip);
                aNode.assembleDestruct(gen);

                gen.mark(skip);
                gen.returnValue();
            }
        });
        aNode.assembleTeardown(generator);
    }

    private static class MyClassLoader extends ClassLoader {
        public MyClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class defineClass(String name, byte[] byteCode) {
            return defineClass(name, byteCode, 0, byteCode.length);
        }
    }
}

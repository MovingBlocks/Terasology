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

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.Map;

/**
 * Class to help generate java classes using asm.
 */
public class ClassGenerator extends ClassVisitor {
    private int fieldCount;
    private Map<String, Type> fieldTypes = Maps.newHashMap();

    private Type type;

    public ClassGenerator(ClassVisitor cv, String className) {
        super(Opcodes.ASM5, cv);

        type = Type.getType(className.replace("\\.", "/"));
        visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, type.toString(), null, Type.getInternalName(CompiledBehaviorTree.class), null);
    }

    public void generateMethod(String method, Consumer<MethodGenerator> closure) {
        Method m = Method.getMethod(method);
        MethodVisitor v = visitMethod(Opcodes.ACC_PUBLIC, m.getName(), m.getDescriptor(), null, null);
        MethodGenerator gen = new MethodGenerator(this, Opcodes.ASM5, v, Opcodes.ACC_PUBLIC, m.getName(), m.getDescriptor());
        closure.accept(gen);

        gen.endMethod();
    }

    public String generateField(Type theType) {
        String name = "field" + fieldCount;
        fieldCount++;
        fieldTypes.put(name, theType);

        visitField(Opcodes.ACC_PUBLIC, name, theType.getDescriptor(), null, null);
        return name;
    }

    public Type getType() {
        return type;
    }

    public Type getFieldType(String name) {
        return fieldTypes.get(name);
    }
}

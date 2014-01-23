/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.reflection.reflect;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Immortius
 */
public class ByteCodeReflectFactory implements ReflectFactory {

    private ClassPool pool;
    private CtClass objectConstructorInterface;

    public ByteCodeReflectFactory() {
        try {
            ClassPool.doPruning = true;
            pool = ClassPool.getDefault();
            objectConstructorInterface = pool.get(ObjectConstructor.class.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException("Error establishing reflection factory", e);
        }
    }

    @Override
    public <T> ObjectConstructor<T> createConstructor(Class<T> type) throws NoSuchMethodException {
        String constructorClassName = type.getName() + "_ReflectConstructor";
        try {
            return (ObjectConstructor<T>) type.getClassLoader().loadClass(constructorClassName).getConstructor().newInstance();
        } catch (ClassNotFoundException ignored) {
            if (Modifier.isPrivate(type.getDeclaredConstructor().getModifiers())) {
                throw new NoSuchMethodException("Constructor for '" + type + "' exists but is private");
            }

            CtClass constructorClass = pool.makeClass(type.getName() + "_ReflectConstructor");
            constructorClass.setInterfaces(new CtClass[]{objectConstructorInterface});
            try {
                CtMethod method = CtNewMethod.make("public Object construct() { return new " + type.getName() + "();}", constructorClass);
                constructorClass.addMethod(method);
                return (ObjectConstructor<T>) (constructorClass.toClass().getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | CannotCompileException e) {
                throw new RuntimeException("Error instantiating constructor object for " + type, e);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error instantiating constructor object for " + type, e);
        }

    }

    @Override
    public <T> FieldAccessor<T, ?> createFieldAccessor(Class<T> ownerType, Field field) throws InaccessibleFieldException {
        return createFieldAccessor(ownerType, field, field.getType());
    }

    @Override
    public <T, U> FieldAccessor<T, U> createFieldAccessor(Class<T> ownerType, Field field, Class<U> fieldType) throws InaccessibleFieldException {
        return new ReflectASMFieldAccessor<>(ownerType, field, fieldType);
    }

    private static class ReflectASMFieldAccessor<T, U> implements FieldAccessor<T, U> {

        private static final int NO_METHOD = -1;

        private MethodAccess methodAccess;
        private int getterIndex = NO_METHOD;
        private int setterIndex = NO_METHOD;
        private FieldAccess fieldAccess;
        private int fieldIndex;

        public ReflectASMFieldAccessor(Class<T> ownerType, Field field, Class<U> fieldType) throws InaccessibleFieldException {
            methodAccess = MethodAccess.get(ownerType);
            Method getter = ReflectionUtil.findGetter(field);
            if (getter != null) {
                getterIndex = methodAccess.getIndex(getter.getName());
            }
            Method setter = ReflectionUtil.findSetter(field);
            if (setter != null) {
                setterIndex = methodAccess.getIndex(setter.getName());
            }

            if (getterIndex == NO_METHOD || setterIndex == NO_METHOD) {
                fieldAccess = FieldAccess.get(ownerType);
                try {
                    fieldIndex = fieldAccess.getIndex(field.getName());
                } catch (IllegalArgumentException e) {
                    throw new InaccessibleFieldException("Failed to create accessor for field '" + field.getName() + "' of type '" + ownerType.getName() + "'", e);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public U getValue(T target) {
            if (getterIndex != NO_METHOD) {
                return (U) methodAccess.invoke(target, getterIndex);
            } else {
                return (U) fieldAccess.get(target, fieldIndex);
            }
        }

        @Override
        public void setValue(T target, U value) {
            if (setterIndex != NO_METHOD) {
                methodAccess.invoke(target, setterIndex, value);
            } else {
                fieldAccess.set(target, fieldIndex, value);
            }
        }
    }
}

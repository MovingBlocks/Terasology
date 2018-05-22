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
package org.terasology.testUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * MethodSignature is the signature for a method (or a constructor) including method name, modifiers, parameters, return type and exceptions.
 * Note: methodSignatures are considered to be equal if two methodSignatures has the same name and the same parameter types.
 */
public class MethodSignature {

    private String name;

    private int modifiers;

    private Class<?>[] parameterTypes;

    private Class<?> returnType;

    private Class<?>[] exceptionTypes;

    /**
     * Register a MethodSignature object for a method.
     * It's used to register a MethodSignature for a API method in API test.
     * @param name name of the method
     * @param parameterTypes parameter types of this method
     * @param modifiers the modifiers of the method
     * @param returnType return type of this method
     * @param exceptionTypes exception types of the method
     */
    public MethodSignature(String name, Class<?>[] parameterTypes, int modifiers, Class<?> returnType, Class<?>[] exceptionTypes) {
        this.name = name;
        this.returnType = returnType;
        this.modifiers = modifiers;
        this.parameterTypes = parameterTypes == null ? new Class[] {} : parameterTypes;
        this.exceptionTypes = exceptionTypes == null ? new Class[] {} : exceptionTypes;
    }

    /**
     * Generate MethodSignature object from a Method object.
     * It's used to get the current method signature for a method.
     * @param method the method
     */
    public MethodSignature(Method method) {
        this.name = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.modifiers = method.getModifiers();
        this.returnType = method.getReturnType();
        this.exceptionTypes = method.getExceptionTypes();
    }

    /**
     * Register a MethodSignature object for a constructor.
     * It's used to register a MethodSignature for a API constructor in API test.
     * @param name the name of the constructor
     * @param modifiers the modifiers of the constructor
     * @param parameterTypes the parameters
     * @param exceptionTypes the array of the exceptions
     */
    public MethodSignature(String name, Class<?>[] parameterTypes, int modifiers, Class<?>[] exceptionTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes == null ? new Class[] {} : parameterTypes;
        this.modifiers = modifiers;
        this.exceptionTypes = exceptionTypes == null ? new Class[] {} : exceptionTypes;
    }

    /**
     * Generate MethodSignature object from a Constructor object.
     * It's used to get the current method signature for a constructor.
     * @param constructor the constructor
     */
    public MethodSignature(Constructor constructor) {
        String name = constructor.getName();
        String[] nameSplit = name.split("\\.");
        this.name = nameSplit[nameSplit.length-1];

        this.parameterTypes = constructor.getParameterTypes();
        this.modifiers = constructor.getModifiers();
        this.exceptionTypes = constructor.getExceptionTypes();
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getExceptionTypes() {
        return exceptionTypes;
    }

    /**
     * Two MethodSignatures are considered to be the same if their names and parameters are the same.
     * This is used to identify which new method is added to an API class
     * @param obj another MethodSignature method
     * @return True if they have the same name and the same parameters
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof MethodSignature))return false;
        MethodSignature other = (MethodSignature) obj;
        if (name.equals(other.getName()) && Arrays.equals(parameterTypes,other.getParameterTypes())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean objectEquals(Object o1, Object o2) {
        if (o1 == o2) return true;
        if ((o1 == null)||(o2 == null)) return false;
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        int result = 7;
        int c = name.hashCode() * 7 + (parameterTypes != null ? Arrays.hashCode(parameterTypes) : 0) * 29;
        result = result * 37 + c;
        return result;
    }
}

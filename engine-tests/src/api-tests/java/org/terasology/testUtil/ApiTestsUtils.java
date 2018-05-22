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
import java.lang.reflect.Modifier;
import java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Some static methods used for API test.
 */
public class ApiTestsUtils {

    /**
     * Compare method signatures between all public methods in an API class and the methods registered in the API test.
     * @param cl The API class
     * @param registeredMethods All public methods registered in the API test
     */
    public static void compareMethods(Class cl, Set<MethodSignature> registeredMethods) {

        for (MethodSignature registeredMethod : registeredMethods) {
            String registeredName = registeredMethod.getName();
            Class[] registeredParams = registeredMethod.getParameterTypes();
            try {
                Method currentMethod = cl.getMethod(registeredName,registeredParams);
                int currentModifiers = currentMethod.getModifiers();
                assertEquals(String.format("Major increase required: method %s changed modifiers from %s to %s",registeredMethod.getName(),Modifier.toString(registeredMethod.getModifiers()),Modifier.toString(currentModifiers)), registeredMethod.getModifiers(),currentModifiers);

                Class currentReturnType = currentMethod.getReturnType();
                assertEquals(String.format("Major increase required: method %s changed return type from %s to %s",registeredMethod.getName(), registeredMethod.getReturnType().toString(),currentReturnType.toString()), registeredMethod.getReturnType(), currentReturnType);

                Class[] currentExceptionTypes = currentMethod.getExceptionTypes();
                assertArrayEquals(String.format("Major increase required: method %s changed exception types from %s to %s", registeredMethod.getName(), Arrays.toString(registeredMethod.getExceptionTypes()), Arrays.toString(currentExceptionTypes)), registeredMethod.getExceptionTypes(),currentExceptionTypes);
            } catch (NoSuchMethodException e) {
                fail(String.format("Major increase required: method %s with parameter types %s has been deleted or changed parameters", registeredMethod, Arrays.toString(registeredParams)));
            }
        }
    }

    /**
     * Print all public methods in a class.
     * It's used to see all the public methods in a class.
     * @param cl the target class
     */
    public static void printPublicMethod(Class cl) {
        Method[] methods = cl.getMethods();
        for (Method method : methods) {
            System.out.println(method.toString());
        }
    }

    /**
     * Compare the number of public methods and the number of the methods registered in the API test.
     * It's used to detect whether a new public method is addede to an API class.
     * @param cl The API class
     * @param registeredMethods All public methods registered in the API test
     */
    public static void compareMethodsNum(Class cl, Set<MethodSignature> registeredMethods) {
        int currentMethodsNum = cl.getMethods().length;
        if (currentMethodsNum < registeredMethods.size()) {
            fail("Major increase required: there is a method being removed, see more details in majorIncreaseTest");
        }
        else if (currentMethodsNum > registeredMethods.size()) {
            for (Method currentMethod : cl.getMethods()) {
                MethodSignature currentSignature = new MethodSignature(currentMethod);
                assertTrue(String.format("Minor increase required: new method %s with parameter types %s added", currentSignature.getName(),Arrays.toString(currentSignature.getParameterTypes())), registeredMethods.contains(currentSignature));
            }
        }
    }

    /**
     * Print all constructors of a class.
     * It's used to see all constructors of a class
     * @param cl the target class
     */
    public static void printConstructors(Class cl) {
        Constructor[] constructors = cl.getConstructors();
        for (Constructor constructor : constructors) {
            System.out.println(constructor.toString());
        }
    }

    /**
     * Compare the signatures between all public constructors and registered constructors in the API test.
     * @param cl The API class
     * @param registeredConstructors All registered constructors of an API class
     */
    public static void compareConstructors(Class cl, Set<MethodSignature> registeredConstructors) {

        for (MethodSignature registeredConstroctor : registeredConstructors) {
            String registeredName = registeredConstroctor.getName();
            Class[] registeredParams = registeredConstroctor.getParameterTypes();
            try {
                Constructor currentMethod = cl.getConstructor(registeredParams);
                int currentModifiers = currentMethod.getModifiers();
                assertEquals(String.format("Major increase required: the constructor changed modifiers from %s to %s",Modifier.toString(registeredConstroctor.getModifiers()),Modifier.toString(currentModifiers)), registeredConstroctor.getModifiers(),currentModifiers);

                Class[] currentExceptionTypes = currentMethod.getExceptionTypes();
                assertArrayEquals(String.format("Major increase required: the constructor changed exception types from %s to %s", Arrays.toString(registeredConstroctor.getExceptionTypes()), Arrays.toString(currentExceptionTypes)), registeredConstroctor.getExceptionTypes(),currentExceptionTypes);
            } catch (NoSuchMethodException e) {
                fail(String.format("Major increase required: the constructor with parameter types %s has been deleted or changed parameters", Arrays.toString(registeredParams)));
            }
        }
    }

    /**
     * Compare the number of constructors and the number of the constructors registered in the API test.
     * @param cl The API class
     * @param registeredConstructors All registered constructors of an API class
     */
    public static void compareConstructorsNum(Class cl, Set<MethodSignature> registeredConstructors) {
        int currentMethodsNum = cl.getConstructors().length;
        if (currentMethodsNum < registeredConstructors.size()) {
            fail("Major increase required: there is a constructor being removed, see details in major IncreaseTest");
        }
        else if (currentMethodsNum > registeredConstructors.size()) {
            for (Constructor currentConstructor : cl.getConstructors()) {
                MethodSignature currentSignature = new MethodSignature(currentConstructor);
                assertTrue(String.format("Minor increase required: new constructor with parameter types %s added", Arrays.toString(currentSignature.getParameterTypes())), registeredConstructors.contains(currentSignature));
            }
        }
    }

    /**
     * Get a modifiable set from an ImmutableSet.
     * @param set the ImmutableSet
     * @return A HashSet contains all contents of the ImmutableSet
     */
    public static Set<MethodSignature> getModifiableSet(Set<MethodSignature> set) {
        Set<MethodSignature> modifiableSet = new HashSet<>();
        for (MethodSignature methodSignature : set) {
            modifiableSet.add(methodSignature);
        }
        return modifiableSet;
    }
}

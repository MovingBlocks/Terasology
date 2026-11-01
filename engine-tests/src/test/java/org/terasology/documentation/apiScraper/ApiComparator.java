// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.documentation.apiScraper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.documentation.apiScraper.util.ApiMethod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects API changes between two instances of a scanned code base.
 */
public final class ApiComparator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiComparator.class);
    private static final String ORIGINAL_API_FILE = "API_file.txt";
    private static final String NEW_API_FILE = "New_API_file.txt";

    private ApiComparator() {

    }

    /**
     * Generates a NEW_API_FILE and compares it with the ORIGINAL_API_FILE to detect major and minor version increases.
     * Major increases: Deletion of class, new public abstract method, new non-default interface method,
     * public method deletion, existing public method's change of parameters types, exception types or return type.
     * Minor increases: Creation of a new class, new non-abstract public methods.
     */
    public static void main(String[] args) throws Exception {

        try (BufferedReader br = new BufferedReader(new FileReader(ORIGINAL_API_FILE))) {

            //Creating a map with the original api's data
            Map<String, Collection<ApiMethod>> originalApi = getApi(br);
            br.close();

            //Generating "New_API_file.txt"
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(NEW_API_FILE)));
            writer.write(CompleteApiScraper.getApi().toString());
            writer.flush();
            writer.close();
            BufferedReader br2 = new BufferedReader(new FileReader(NEW_API_FILE));

            //Creating a map with the new api's data
            Map<String, Collection<ApiMethod>> newApi = getApi(br2);
            br2.close();

            //Begins comparison and increases report
            LOGGER.info("=================================================================");
            checkClassAdditionAndDeletion(originalApi, newApi);
            checkMethodChanges(originalApi, newApi);
            LOGGER.info("REPORT FINISHED");
        }
    }

    /**
     * Reads an api file and puts its information in a map to be used in the api comparison.
     * @param br BufferedReader containing an api file content
     * @return Map of api classes and interfaces as keys corresponding to lists of ApiMethods as values
     * @throws Exception if the readLine fails.
     */
    private static Map<String, Collection<ApiMethod>> getApi(BufferedReader br) throws Exception {
        String line = br.readLine();
        Map<String, Collection<ApiMethod>> api = new HashMap<>();
        while (line != null) {
            if (line.startsWith("*")) {
                if (line.endsWith("(PACKAGE)")) {
                    line = br.readLine();
                    continue;
                }
                String className = line;
                String aux;
                api.put(className, new ArrayList<>());
                ApiMethod method;
                aux = br.readLine();
                while ((aux != null && (aux.endsWith("(METHOD)")
                        || aux.endsWith("(CONSTRUCTOR)")
                        || aux.endsWith("(ABSTRACT METHOD)")
                        || aux.endsWith("(DEFAULT METHOD)")))) {

                    //Checks if its a method or constructor
                    if (aux.endsWith("(METHOD)") || aux.endsWith("(ABSTRACT METHOD)") || aux.endsWith("(DEFAULT METHOD)")) {
                        String returnType = br.readLine();
                        String parameters = br.readLine();
                        String exceptionType = br.readLine();
                        method = new ApiMethod(className, aux, returnType, exceptionType, parameters);
                    } else {
                        String returnType = "";
                        String parameters = br.readLine();
                        String exceptionType = "";
                        method = new ApiMethod(className, aux, returnType, exceptionType, parameters);
                    }


                    api.get(className).add(method);
                    aux = br.readLine();
                }
                line = aux;

            } else {
                line = br.readLine();
            }
        }
        return api;
    }

    private static void checkClassAdditionAndDeletion(Map<String, Collection<ApiMethod>> originalApi, Map<String, Collection<ApiMethod>> newApi) {
        LOGGER.info("Checking Class Addition and Deletion");
        for (String className : originalApi.keySet()) {
            if (!newApi.containsKey(className)) {
                LOGGER.info("MAJOR INCREASE, DELETION OF {}", className);
            }
        }

        for (String className : newApi.keySet()) {
            if (!originalApi.containsKey(className)) {
                LOGGER.info("MINOR INCREASE, ADDITION OF {}", className);
            }
        }
    }

    /**
     * Checks creation and deletion of methods, as well as existing method changes.
     * @param originalApi the original api generated from ORIGINAL_API_FILE
     * @param newApi the new apí generated from NEW_API_FILE
     */
    private static void checkMethodChanges(Map<String, Collection<ApiMethod>> originalApi,
                                           Map<String, Collection<ApiMethod>> newApi) {
        LOGGER.info("Checking Method Changes");
        Collection<ApiMethod> originalMethods;
        Collection<ApiMethod> newMethods;
        for (String className : originalApi.keySet()) {

            originalMethods = originalApi.get(className);
            newMethods = newApi.get(className);
            if (newMethods == null) {
                continue;
            }
            checkMethodDeletion(originalMethods, newMethods);

            for (ApiMethod method2 : newMethods) {

                boolean found = false; // if found, the method is an existing one or a new overloaded method
                for (ApiMethod method1 : originalMethods) {
                    if (method1.getName().equals(method2.getName())) {

                        ApiMethod auxMethod = getMethodWithSameNameAndParameters(method2, originalMethods);
                        if (auxMethod.getName().equals("")) {
                            ApiMethod auxMethod2 = getMethodWithSameNameAndParameters(method1, newMethods);
                            if (auxMethod2.getName().equals("")) {
                                checkMethodIncrease(method1, method2);
                            } else if (isInterfaceOrAbstract(method2.getClassName())) {
                                LOGGER.info("MINOR INCREASE, NEW OVERLOADED METHOD {} ON {}\nNEW PARAMETERS: {}",
                                        method2.getName(),  method2.getClassName(), method2.getParametersType());
                                LOGGER.info("=================================================================");
                            }

                        } else {
                            checkMethodIncrease(auxMethod, method2);

                        }
                        found = true;
                    }
                }
                if (!found) {
                    if (isInterfaceOrAbstract(method2.getClassName())) {
                        if (method2.getName().endsWith("(ABSTRACT METHOD)")) {
                            LOGGER.info("MAJOR INCREASE, NEW ABSTRACT METHOD {} ON {}", method2.getName(), method2.getClassName());
                        } else {
                            String minorOrMajor;
                            if (method2.getClassName().endsWith("(INTERFACE)")) {
                                if (method2.getName().endsWith("(DEFAULT METHOD)")) {
                                    minorOrMajor = "MINOR";
                                } else {
                                    minorOrMajor = "MAJOR";
                                }
                            } else {
                                minorOrMajor = "MINOR";
                            }
                            LOGGER.info(minorOrMajor + " INCREASE, NEW METHOD " + method2.getName() + " ON " + method2.getClassName());
                        }
                    } else {
                        LOGGER.info("MINOR INCREASE, NEW METHOD {} ON {}", method2.getName(), method2.getClassName());
                    }
                    LOGGER.info("=================================================================");
                }
            }
        }
    }

    private static void checkMethodDeletion(Collection<ApiMethod> originalMethods, Collection<ApiMethod> newMethods) {
        List<String> checkedMethods = new ArrayList<>();
        for (ApiMethod method1 : originalMethods) {

            boolean found = false;
            List<ApiMethod> newMethodsWithSameName = new ArrayList<>();
            List<ApiMethod> originalMethodsWithSameName = new ArrayList<>();
            for (ApiMethod method2 : newMethods) {
                if (method1.getName().equals(method2.getName())) {
                    found = true;
                    newMethodsWithSameName.add(method2);
                }
            }

            //this checks the deletion of an overloaded method
            if (found && !checkedMethods.contains(method1.getName())) {
                for (ApiMethod oMethod : originalMethods) {
                    if (oMethod.getName().equals(method1.getName())) {
                        originalMethodsWithSameName.add(oMethod);
                    }
                }
                if ((originalMethodsWithSameName.size() - newMethodsWithSameName.size()) > 0) {
                    for (ApiMethod method : originalMethodsWithSameName) {
                        ApiMethod result = getMethodWithSameNameAndParameters(method, newMethodsWithSameName);
                        if (result.getName().equals("")) {
                            checkedMethods.add(method.getName());
                            LOGGER.info("MAJOR INCREASE, OVERLOADED METHOD DELETION:  " + method.getName()
                                    + " ON " + method.getClassName() + "\nPARAMETERS: " + method.getParametersType());

                        }
                    }
                }
            }
            if (!found) {
                LOGGER.info("MAJOR INCREASE, METHOD DELETION:  " + method1.getName() + " ON " + method1.getClassName());
            }
        }
    }

    private static boolean isInterfaceOrAbstract(String className) {
        return (className.endsWith("(ABSTRACT CLASS)") || className.endsWith("(INTERFACE)"));
    }

    /**
     * Compares a not overloaded method in the newApi and originalApi to notify parameter type, return type or
     * exception type changes.
     * @param method1 a not overloaded method from the originalApi, with the same name as method2
     * @param method2 a not overloaded method from the newApi, with the same name as method1
     */
    private static void checkMethodIncrease(ApiMethod method1, ApiMethod method2) {
        check(method1.getReturnType(), method2.getReturnType(), method1.getName(), method1.getClassName());
        check(method1.getParametersType(), method2.getParametersType(), method1.getName(), method1.getClassName());
        check(method1.getExceptionType(), method2.getExceptionType(), method1.getName(), method1.getClassName());
    }

    /**
     * Compares a method's field in the newApi and originalApi. This field can be, return, parameter or exception type.
     * @param s1 field to be compared from a method in the originalApi
     * @param s2 field to be compared from a method in the newApi
     * @param methodName name of the method to have it's field being compared
     * @param className the name of the class the have the method
     */
    private static void check(String s1, String s2, String methodName, String className) {
        if (!s1.equals(s2)) {
            LOGGER.info("MAJOR INCREASE ON : " + methodName + " " + className);
            LOGGER.info("ORIGINAL: " + s1);
            LOGGER.info("NEW: " + s2);
            LOGGER.info("=================================================================");
        }
    }

    /**
     * Tries to find a method with the same name and parameter type as 'method' in a collection of methods.
     * @param method the method used in the search
     * @param methods the collection of methods
     * @return method with same name and parameter type if 'method' exists, otherwise a new ApiMethod with empty attributes
     */
    private static ApiMethod getMethodWithSameNameAndParameters(ApiMethod method, Collection<ApiMethod> methods) {
        for (ApiMethod m : methods) {
            if (m.getName().equals(method.getName()) && m.getParametersType().equals(method.getParametersType())) {
                return m;
            }
        }
        return new ApiMethod("", "", "", "", "");
    }
}

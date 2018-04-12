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
package org.terasology.documentation.apiScraper;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.terasology.documentation.apiScraper.util.ApiMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class ApiComparator {
    public static void main(String[] args) throws Exception {

        try(BufferedReader br = new BufferedReader(new FileReader("API_file.txt"))){

            HashMap<String, Collection<ApiMethod>> originalApi = getApi(br);
            br.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("New_API_file.txt")));
            writer.write(CompleteApiScraper.getApi().toString());
            writer.flush();
            writer.close();
            BufferedReader br2 = new BufferedReader(new FileReader("New_API_file.txt"));
            HashMap<String, Collection<ApiMethod>> newApi = getApi(br2);
            br2.close();

            System.out.println("=================================================================");
            checkClassAdditionAndDeletion(originalApi, newApi);
            checkMethodChanges(originalApi, newApi);
            System.out.println("REPORT FINISHED");
        }

    }

    private static HashMap<String, Collection<ApiMethod>> getApi(BufferedReader br) throws Exception{
        String line = br.readLine();
        HashMap<String, Collection<ApiMethod>> api = new HashMap<>();
        while(line != null){
            if(line.startsWith("*")){
                if (line.endsWith("(PACKAGE)")){
                    line = br.readLine();
                    continue;
                }
                String className = line;
                String aux;
                api.put(className, new ArrayList<>());
                ApiMethod method;
                while((aux = br.readLine()) != null && (aux.endsWith("(METHOD)")
                        || aux.endsWith("(CONSTRUCTOR)")
                        || aux.endsWith("(ABSTRACT METHOD)"))){
                    if(aux.endsWith("(METHOD)") || aux.endsWith("(ABSTRACT METHOD)")){
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
                }
                line = aux;

            } else {
                line = br.readLine();
            }
        }
        return api;
    }

    private static void checkClassAdditionAndDeletion(HashMap<String, Collection<ApiMethod>> originalApi, HashMap<String, Collection<ApiMethod>> newApi){
        System.out.println("Checking Class Addition and Deletion");
        for(String className : originalApi.keySet()){
            if(!newApi.containsKey(className)){
                System.out.println("MAJOR INCREASE, DELETION OF " + className);
            }
        }

        for(String className : newApi.keySet()){
            if(!originalApi.containsKey(className)){
                System.out.println("MINOR INCREASE, ADDITION OF " + className);
            }
        }

    }

    private static void checkMethodChanges(HashMap<String, Collection<ApiMethod>> originalApi,
                                           HashMap<String, Collection<ApiMethod>> newApi){
        System.out.println("Checking Method Changes");
        Collection<ApiMethod> originalMethods;
        Collection<ApiMethod> newMethods;
        for(String className : originalApi.keySet()){

            originalMethods = originalApi.get(className);
            newMethods = newApi.get(className);
            if(newMethods == null) continue;
            checkMethodDeletion(originalMethods, newMethods);

            for(ApiMethod method2 : newMethods){

                boolean found = false;
                for(ApiMethod method1 : originalMethods){
                    if(method1.getName().equals(method2.getName())){

                        ApiMethod auxMethod = getMethodWithSameNameAndParameters(method2, originalMethods);
                        if(auxMethod.getName().equals("")){
                            ApiMethod auxMethod2 = getMethodWithSameNameAndParameters(method1, newMethods);
                            if(auxMethod2.getName().equals("")){
                                checkMethodIncrease(method1, method2);
                            } else if (isInterfaceOrAbstract(method2.getClassName())){
                                System.out.println("MINOR INCREASE, NEW OVERLOADED METHOD " + method2.getName() +
                                        " ON " + method2.getClassName() + "\nNEW PARAMETERS: " + method2.getParametersType());
                                System.out.println("=================================================================");
                            }

                        } else {
                            checkMethodIncrease(auxMethod, method2);

                        }
                        found = true;
                    }
                }
                if(! found && isInterfaceOrAbstract(method2.getClassName())){
                    if(method2.getName().endsWith("(ABSTRACT METHOD)")){
                        System.out.println("MAJOR INCREASE, NEW ABSTRACT METHOD " + method2.getName() + " ON " + method2.getClassName());
                    }else{
                        System.out.println("MINOR INCREASE, NEW METHOD " + method2.getName() + " ON " + method2.getClassName());
                    }
                    System.out.println("=================================================================");
                }
            }

        }

    }

    private static void checkMethodDeletion(Collection<ApiMethod>originalMethods, Collection<ApiMethod> newMethods){
        List<String> checkedMethods = new ArrayList<>();
        for(ApiMethod method1 : originalMethods){

            boolean found = false;
            List<ApiMethod> newMethodsWithSameName = new ArrayList<>();
            List<ApiMethod> originalMethodsWithSameName = new ArrayList<>();
            for(ApiMethod method2 : newMethods){
                if(method1.getName().equals(method2.getName())){
                    found = true;
                    newMethodsWithSameName.add(method2);
                }
            }

            if(found && !checkedMethods.contains(method1.getName())){
                for(ApiMethod oMethod : originalMethods){
                    if(oMethod.getName().equals(method1.getName())){
                        originalMethodsWithSameName.add(oMethod);
                    }
                }
                if((originalMethodsWithSameName.size() - newMethodsWithSameName.size()) > 0){
                    for(ApiMethod method : originalMethodsWithSameName){
                        ApiMethod result = getMethodWithSameNameAndParameters(method, newMethodsWithSameName);
                        if (result.getName().equals("")){
                            checkedMethods.add(method.getName());
                            System.out.println("MAJOR INCREASE, OVERLOADED METHOD DELETION:  " + method.getName()
                                    + " ON " + method.getClassName() + "\nPARAMETERS: " + method.getParametersType());

                        }
                    }
                }
            }
            if(!found){
                System.out.println("MAJOR INCREASE, METHOD DELETION:  " + method1.getName() + " ON " + method1.getClassName());
            }
        }

    }

    private static boolean isInterfaceOrAbstract(String className){
        return (className.endsWith("(ABSTRACT CLASS)") || className.endsWith("(INTERFACE)"));
    }

    private static void checkMethodIncrease(ApiMethod method1, ApiMethod method2){
        check(method1.getReturnType(), method2.getReturnType(), method1.getName(), method1.getClassName());
        check(method1.getParametersType(), method2.getParametersType(), method1.getName(), method1.getClassName());
        check(method1.getExceptionType(), method2.getExceptionType(), method1.getName(), method1.getClassName());
    }

    private static void check(String s1, String s2, String methodName, String className){
        if(!s1.equals(s2)){
            System.out.println("MAJOR INCREASE ON : " + methodName + " " + className);
            System.out.println("ORIGINAL: " + s1);
            System.out.println("NEW: " + s2);
            System.out.println("=================================================================");
        }

    }

    private static ApiMethod getMethodWithSameNameAndParameters(ApiMethod method, Collection<ApiMethod> originalMethods){
        for(ApiMethod m : originalMethods){
            if(m.getName().equals(method.getName()) && m.getParametersType().equals(method.getParametersType())){
                return m;
            }
        }
        return new ApiMethod("","","","","");

    }


}

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

/**
 * Created by iaron on 04/02/2018.
 */
public class ApiComparator {
    public static void main(String[] args) throws Exception {

        try(BufferedReader br = new BufferedReader(new FileReader("API_file.txt"))){

            Multimap<String, ApiMethod> originalApi = getApi(br);
            br.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("New_API_file.txt")));
            writer.write(CompleteApiScraper.getApi().toString());
            writer.flush();
            writer.close();
            BufferedReader br2 = new BufferedReader(new FileReader("New_API_file.txt"));
            Multimap<String, ApiMethod> newApi = getApi(br2);
            br2.close();

            checkIncrease(originalApi, newApi);
            System.out.println("REPORT FINISHED");
        }

    }

    private static Multimap<String, ApiMethod> getApi(BufferedReader br) throws Exception{
        String line = br.readLine();
        Multimap<String, ApiMethod> api = Multimaps.newMultimap(new HashMap<String, Collection<ApiMethod>>(), ArrayList::new);
        while(line != null){
            if(line.startsWith("*")){
                if (line.endsWith("(PACKAGE)")){
                    line = br.readLine();
                    continue;
                }
                String className = line;
                String aux;
                ApiMethod aux_method = new ApiMethod();
                api.put(className, aux_method);
                while((aux = br.readLine()) != null && aux.endsWith("(METHOD)")){
                    String methodName = aux;
                    String returnType = br.readLine();
                    String parameters = br.readLine();
                    String exceptionType = br.readLine();
                    ApiMethod method = new ApiMethod(className, methodName, returnType, exceptionType, parameters);

                    api.put(className, method);
                }
                line = aux;

            } else {
                line = br.readLine();
            }
        }
        return api;
    }

    private static void checkIncrease(Multimap<String, ApiMethod> originalApi, Multimap<String, ApiMethod> newApi){
        Collection<ApiMethod> originalMethods;
        Collection<ApiMethod> newMethods;
        for(String className : originalApi.keySet()){

            originalMethods = originalApi.get(className);
            newMethods = newApi.get(className);


                for(ApiMethod method2 : newMethods){

                    boolean found = false;
                    for(ApiMethod method1 : originalMethods){
                        if(className.equals("* org.terasology.particles.components.generators.VelocityRangeGeneratorComponent (CLASS)")){
                            found = false;
                        }
                        if(method1.getName().equals(method2.getName())){

                            ApiMethod auxMethod = getMethodWithSameNameAndParameters(method2, originalMethods);
                            if(!method1.getName().equals(ApiMethod.DEFAULT_VALUE) && auxMethod.getName().equals(ApiMethod.DEFAULT_VALUE)){
                                ApiMethod auxMethod2 = getMethodWithSameNameAndParameters(method1, newMethods);
                                if(!method2.getName().equals(ApiMethod.DEFAULT_VALUE) && auxMethod2.getName().equals(ApiMethod.DEFAULT_VALUE)){
                                    checkIncrease(method1, method2);
                                } else{
                                    System.out.println("MINOR INCREASE, NEW OVERLOADED METHOD " + method2.getName() +
                                            " ON " + method2.getClassName() + "\nNEW PARAMETERS: " + method2.getParametersType());
                                    System.out.println("=================================================================");
                                }

                            } else {
                                checkIncrease(auxMethod, method2);

                            }
                            found = true;
                        }
                    }
                    if(! found && !method2.getName().equals(ApiMethod.DEFAULT_VALUE)){
                        System.out.println("MINOR INCREASE, NEW METHOD " + method2.getName() + " ON " + method2.getClassName());
                        System.out.println("=================================================================");
                    }
                }

        }

    }

    private static void checkIncrease(ApiMethod method1, ApiMethod method2){
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
        return new ApiMethod();

    }


}

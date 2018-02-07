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
package org.terasology.documentation.apiScraper;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.ArrayListMultimap;
import org.terasology.engine.module.ExternalApiWhitelist;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.sandbox.API;
import org.terasology.testUtil.ModuleManagerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enumerates all classes and packages that are annotated with {@link API}.
 */
public final class CompleteApiScraper {
    private CompleteApiScraper() {
        // Private constructor, utility class
    }

    /**
     *
     * @return Project's Packages, Interfaces, Classes and Methods
     * @throws Exception if the module environment cannot be loaded
     */
    public static StringBuffer getApi() throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleEnvironment environment = moduleManager.getEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Multimap<String, String> api = Multimaps.newMultimap(new HashMap<String, Collection<String>>(), ArrayList::new);

        for (Class<?> apiClass : environment.getTypesAnnotatedWith(API.class)) {
            //System.out.println("Processing: " + apiClass);
            boolean isPackage = apiClass.isSynthetic();
            URL location;
            String category;
            String apiPackage = "";
            if (isPackage) {
                apiPackage = apiClass.getPackage().getName();
                location = classLoader.getResource(apiPackage.replace('.', '/'));
            } else {

                location = apiClass.getResource('/' + apiClass.getName().replace('.', '/') + ".class");
            }

            if (location == null) {
                System.out.println("Failed to get a class/package location, skipping " + apiClass);
                continue;
            }

            switch (location.getProtocol()) {
                case "jar" :

                    // Find out what jar it came from and consider that the category
                    String categoryFragment = location.getPath();
                    //System.out.println("category fragment as path: " + categoryFragment);
                    int bang = categoryFragment.lastIndexOf("!");
                    int hyphen = categoryFragment.lastIndexOf("-", bang);
                    int slash = categoryFragment.lastIndexOf("/", hyphen);
                    category = categoryFragment.substring(slash + 1, hyphen);
                    //System.out.println("category fragment pared down: " + category);
                    addToApi(isPackage, category, apiPackage, apiClass, api);
                    break;

                case "file" :
                    // If file based we know it is local so organize it like that
                    category = "terasology engine";
                    addToApi(isPackage, category, apiPackage, apiClass, api);
                    break;

                default :
                    System.out.println("Unknown protocol for: " + apiClass + ", came from " + location);
            }
        }
        api.putAll("external", ExternalApiWhitelist.CLASSES.stream()
                .map(clazz->clazz.getName() + " (CLASS)").collect(Collectors.toSet()));
        api.putAll("external", ExternalApiWhitelist.PACKAGES.stream()
                .map(packagee->packagee + " (PACKAGE)").collect(Collectors.toSet()));


        StringBuffer stringApi = new StringBuffer();

        stringApi.append("# Modding API:\n");
        for (String key : api.keySet()) {
            stringApi.append("## " + key + "\n");
            for (String value : api.get(key)) {
                stringApi.append("* " + value + "\n");
            }
            stringApi.append("\n");
        }

        return stringApi;
    }

    private static void addToApi(boolean isPackage, String category, String apiPackage, Class<?> apiClass, Multimap<String, String> api){
        if (isPackage) {
            //System.out.println("Local Package: " + apiPackage + ", came from " + location);
            api.put(category, apiPackage + " (PACKAGE)");
        } else {
            //System.out.println("Local Class: " + apiClass + ", came from " + location);
            api.put(category, apiClass.getName() + (apiClass.isInterface() ? " (INTERFACE)" : " (CLASS)"));
            Method[] methods = apiClass.getDeclaredMethods();
            for(int i = 0; i < methods.length; i++){
                if(!methods[i].isDefault() && !methods[i].isBridge() && !methods[i].isSynthetic()){
                    api.put(category, " - " + methods[i].getName() +  " (METHOD)");
                    api.put(category, " -- " + methods[i].getReturnType() +  " (RETURN)");
                    api.put(category, " -- " + Arrays.toString(methods[i].getParameterTypes()) +  " (PARAMETERS)");
                    api.put(category, " -- " + Arrays.toString(methods[i].getExceptionTypes()) +  " (EXCEPTIONS)");
                }
            }
        }

    }
}

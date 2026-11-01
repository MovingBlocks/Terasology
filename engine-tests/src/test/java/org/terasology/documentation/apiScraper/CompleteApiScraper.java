// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.documentation.apiScraper;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.context.annotation.API;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Enumerates all classes, interfaces and packages that are annotated with {@link API} and their public methods and
 * constructors.
 */
final class CompleteApiScraper {

    private static final String TERASOLOGY_API_CLASS_CATEGORY = "terasology engine";
    private static final String EXTERNAL = "external";

    private static final Logger logger = LoggerFactory.getLogger(CompleteApiScraper.class);

    private CompleteApiScraper() {
        // Private constructor, utility class
    }

    /**
     *
     * @return Project's Packages, Interfaces, Classes and Methods
     * @throws Exception if the module environment cannot be loaded
     */
    static StringBuffer getApi() throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleEnvironment environment = moduleManager.getEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Multimap<String, String> api = Multimaps.newMultimap(new HashMap<String, Collection<String>>(), ArrayList::new);

        for (Class<?> apiClass : environment.getTypesAnnotatedWith(API.class)) {
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
                logger.error("Failed to get a class/package location, skipping {}", apiClass);
                continue;
            }

            switch (location.getProtocol()) {
                case "jar" :

                    // Find out what jar it came from and consider that the category
                    String categoryFragment = location.getPath();

                    int bang = categoryFragment.lastIndexOf("!");
                    int hyphen = categoryFragment.lastIndexOf("-", bang);
                    int slash = categoryFragment.lastIndexOf("/", hyphen);
                    category = categoryFragment.substring(slash + 1, hyphen);


                    if (isPackage) {
                        api.put(category, apiPackage + " (PACKAGE)");
                    } else {
                        addToApi(category, apiClass, api);
                    }
                    break;

                case "file" :
                    // If file based we know it is local so organize it like that
                    category = TERASOLOGY_API_CLASS_CATEGORY;
                    if (isPackage) {
                        api.put(category, apiPackage + " (PACKAGE)");
                    } else {
                        addToApi(category, apiClass, api);
                    }
                    break;

                default :
                    logger.error("Unknown protocol for: {} , came from {}", apiClass, location);
            }
        }
        api.putAll(EXTERNAL, ExternalApiWhitelist.CLASSES.stream()
                .map(clazz -> clazz.getName() + " (CLASS)").collect(Collectors.toSet()));
        api.putAll(EXTERNAL, ExternalApiWhitelist.PACKAGES.stream()
                .map(packagee -> packagee + " (PACKAGE)").collect(Collectors.toSet()));

        //Puts the information in the StringBuffer
        StringBuffer stringApi = new StringBuffer();
        stringApi.append("# Modding API:\n");
        for (String key : api.keySet()) {
            stringApi.append("## ");
            stringApi.append(key);
            stringApi.append("\n");
            for (String value : api.get(key)) {
                stringApi.append("* ");
                stringApi.append(value);
                stringApi.append("\n");
            }
            stringApi.append("\n");
        }
        return stringApi;
    }

    /**
     * Adds interface or class and their methods and constructors to api
     * are also added.
     * @param category where the apiClass belongs
     * @param apiClass the class or interface to be added
     * @param api that maps category to classes/interface/methods
     */
    private static void addToApi(String category, Class<?> apiClass, Multimap<String, String> api) {

        String className = apiClass.getName();
        String type;
        if (apiClass.isInterface()) {
            type = " (INTERFACE)";
        } else {
            int modifier = apiClass.getModifiers();
            if (Modifier.isAbstract(modifier)) {
                type = " (ABSTRACT CLASS)";
            } else {
                type = " (CLASS)";
            }
        }
        api.put(category, className + type);

        //Add current apiClass's constructors
        Constructor[] constructors = apiClass.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            api.put(category, " - " + constructor.getName() +  " (CONSTRUCTOR)");
            api.put(category, " -- " + Arrays.toString(constructor.getParameterTypes()) +  " (PARAMETERS)");
        }

        //Add current apiClass's methods
        Method[] methods = apiClass.getDeclaredMethods();
        for (Method method: methods) {
            if (!method.isDefault() && !method.isBridge() && !method.isSynthetic()) {

                //Check if it's an abstract method
                int modifier = method.getModifiers();
                if (Modifier.isAbstract(modifier)) {
                    type = " (ABSTRACT METHOD)";
                } else {
                    type = " (METHOD)";
                }

                //Adds method's information
                api.put(category, " - " + method.getName() +  type);
                api.put(category, " -- " + method.getReturnType() +  " (RETURN)");
                api.put(category, " -- " + Arrays.toString(method.getParameterTypes()) +  " (PARAMETERS)");
                api.put(category, " -- " + Arrays.toString(method.getExceptionTypes()) +  " (EXCEPTIONS)");
            } else if (method.isDefault() && apiClass.isInterface()) {
                api.put(category, " - " + method.getName() +  " (DEFAULT METHOD)");
                api.put(category, " -- " + method.getReturnType() +  " (RETURN)");
                api.put(category, " -- " + Arrays.toString(method.getParameterTypes()) +  " (PARAMETERS)");
                api.put(category, " -- " + Arrays.toString(method.getExceptionTypes()) +  " (EXCEPTIONS)");
            }

        }
    }
}

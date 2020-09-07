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
package org.terasology.documentation;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import org.terasology.engine.module.ExternalApiWhitelist;
import org.terasology.engine.module.ModuleManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.testUtil.ModuleManagerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Enumerates all classes and packages that are annotated with {@link API}.
 */
public final class ApiScraper {
    private ApiScraper() {
        // Private constructor, utility class
    }

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleEnvironment environment = moduleManager.getEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        SortedSetMultimap<String, String> sortedApi = Multimaps.newSortedSetMultimap(new HashMap<>(), TreeSet::new);

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

                    if (isPackage) {
                        //System.out.println("Jar-based Package: " + apiPackage + ", came from " + location);
                        sortedApi.put(category, apiPackage + " (PACKAGE)");
                    } else {
                        //System.out.println("Jar-based Class: " + apiClass + ", came from " + location);
                        sortedApi.put(category, apiClass.getName() + (apiClass.isInterface() ? " (INTERFACE)" : " (CLASS)"));
                    }

                    break;

                case "file" :

                    // If file based we know it is local so organize it like that
                    category = "terasology engine";

                    if (isPackage) {
                        //System.out.println("Local Package: " + apiPackage + ", came from " + location);
                        sortedApi.put(category, apiPackage + " (PACKAGE)");
                    } else {
                        //System.out.println("Local Class: " + apiClass + ", came from " + location);
                        sortedApi.put(category, apiClass.getName() + (apiClass.isInterface() ? " (INTERFACE)" : " (CLASS)"));
                    }

                    break;

                default :
                    System.out.println("Unknown protocol for: " + apiClass + ", came from " + location);
            }
        }
        sortedApi.putAll("external", ExternalApiWhitelist.CLASSES.stream()
                .map(clazz->clazz.getName() + " (CLASS)").collect(Collectors.toSet()));
        sortedApi.putAll("external", ExternalApiWhitelist.PACKAGES.stream()
                .map(packagee->packagee + " (PACKAGE)").collect(Collectors.toSet()));

        System.out.println("# Modding API:\n");
        for (String key : sortedApi.keySet()) {
            System.out.println("## " + key + "\n");
            for (String value : sortedApi.get(key)) {
                System.out.println("* " + value);
            }
            System.out.println("");
        }
    }
}

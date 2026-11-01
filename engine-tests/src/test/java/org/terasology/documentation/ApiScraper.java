// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.documentation;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.context.annotation.API;

import java.net.URL;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Enumerates all classes and packages that are annotated with {@link API}.
 */
@SuppressWarnings("PMD.SystemPrintln") // main entrypoint used to generate documentation
public final class ApiScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiScraper.class);

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
            LOGGER.debug("Processing: {}", apiClass);
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
                LOGGER.info("Failed to get a class/package location, skipping {}", apiClass);
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
                    LOGGER.info("Unknown protocol for: {}, came from {}", apiClass, location);
            }
        }
        sortedApi.putAll("external", ExternalApiWhitelist.CLASSES.stream()
                .map(clazz -> clazz.getName() + " (CLASS)").collect(Collectors.toSet()));
        sortedApi.putAll("external", ExternalApiWhitelist.PACKAGES.stream()
                .map(packagee -> packagee + " (PACKAGE)").collect(Collectors.toSet()));

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

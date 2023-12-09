// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.documentation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.DefaultBindings;
import org.terasology.input.Input;
import org.terasology.input.InputCategory;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.engine.testUtil.ModuleManagerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates all default key bindings and writes them sorted by ID to the console
 */
@SuppressWarnings("PMD.SystemPrintln") // main entrypoint used to generate documentation
public final class BindingScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BindingScraper.class);

    private BindingScraper() {
        // Utility class, no instances
    }

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();

        // Holds normal input mappings where there is only one key
        Multimap<InputCategory, String> categories = ArrayListMultimap.create();
        Multimap<String, Input> keys = ArrayListMultimap.create();
        Map<String, String> desc = new HashMap<>();

        for (Class<?> holdingType : moduleManager.getEnvironment().getTypesAnnotatedWith(InputCategory.class)) {
            InputCategory inputCategory = holdingType.getAnnotation(InputCategory.class);
            categories.put(inputCategory, null);
            for (String button : inputCategory.ordering()) {
                categories.put(inputCategory, button);
            }
        }

        for (Class<?> buttonEvent : moduleManager.getEnvironment().getTypesAnnotatedWith(RegisterBindButton.class)) {
            DefaultBinding defBinding = buttonEvent.getAnnotation(DefaultBinding.class);
            RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);

            String cat = info.category();
            String id = "engine:" + info.id();
            desc.put(id, info.description());

            if (cat.isEmpty()) {
                InputCategory inputCategory = findEntry(categories, id);
                if (inputCategory == null) {
                    LOGGER.info("Invalid category for: {}", info.id());
                }
            } else {
                InputCategory inputCategory = findCategory(categories, cat);
                if (inputCategory != null) {
                    categories.put(inputCategory, id);
                } else {
                    LOGGER.info("Invalid category for: {}", info.id());
                }
            }

            if (defBinding != null) {
                // This handles bindings with just one key
                Input input = defBinding.type().getInput(defBinding.id());
                keys.put(id, input);
            } else {
                // See if there is a multi-mapping for this button
                DefaultBindings multiBinding = buttonEvent.getAnnotation(DefaultBindings.class);

                // Annotation math magic. We're expecting a DefaultBindings containing one DefaultBinding pair
                if (multiBinding != null && multiBinding.value().length == 2) {
                    DefaultBinding[] bindings = multiBinding.value();
                    Input primary = bindings[0].type().getInput(bindings[0].id());
                    Input secondary = bindings[1].type().getInput(bindings[1].id());
                    keys.put(id, primary);
                    keys.put(id, secondary);
                }
            }
        }

        for (InputCategory row : categories.keySet()) {
            System.out.println("# " + row.displayName());

            categories.get(row).stream().filter(entry -> entry != null).forEach(entry ->
                    System.out.println(desc.get(entry) + ": " + keys.get(entry)));
        }
    }

    private static InputCategory findCategory(Multimap<InputCategory, String> categories, String id) {
        for (InputCategory x : categories.keySet()) {
            if (x.id().equals(id)) {
                return x;
            }
        }
        return null;
    }

    private static InputCategory findEntry(Multimap<InputCategory, String> categories, String id) {
        for (InputCategory x : categories.keySet()) {
            if (categories.get(x).contains(id)) {
                return x;
            }
        }
        return null;
    }
}

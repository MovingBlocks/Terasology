/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.documentation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeBasedTable;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.DefaultBinding;
import org.terasology.input.DefaultBindings;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;
import org.terasology.testUtil.ModuleManagerFactory;

import java.util.Map.Entry;

/**
 * Enumerates all default key bindings and writes them sorted by ID to the console
 */
public final class BindingScraper {

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
        TreeBasedTable<String, Input, String> keyTable = TreeBasedTable.create(
                String.CASE_INSENSITIVE_ORDER,
                (i1, i2) -> Integer.compare(i1.getId(), i2.getId()));

        // Holds input mappings that have both a primary and secondary key
        SetMultimap<String, String> multiKeys = HashMultimap.create();

        for (Class<?> buttonEvent : moduleManager.getEnvironment().getTypesAnnotatedWith(RegisterBindButton.class)) {
            DefaultBinding defBinding = buttonEvent.getAnnotation(DefaultBinding.class);
            RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
            if (defBinding != null) {
                // This handles bindings with just one key
                if (defBinding.type() == InputType.KEY) {
                    Input input = InputType.KEY.getInput(defBinding.id());
                    keyTable.put(info.category(), input, info.description());
                }
            } else {
                // See if there is a multi-mapping for this button
                DefaultBindings[] multiBinding = buttonEvent.getAnnotationsByType(DefaultBindings.class);

                // Annotation math magic. We're expecting a DefaultBindings containing one DefaultBinding pair
                if (multiBinding != null && multiBinding.length == 1 && multiBinding[0].value().length == 2) {
                    String primaryKey = InputType.KEY.getInput(multiBinding[0].value()[0].id()).getDisplayName();
                    String secondaryKey = InputType.KEY.getInput(multiBinding[0].value()[1].id()).getDisplayName();
                    multiKeys.put(info.category(), "`" + primaryKey + "` OR `" + secondaryKey + "` : " + info.description());
                }
            }
        }

        for (String row : keyTable.rowKeySet()) {
            // Print the category. Some keys may not have one, group those as uncategorized
            if (row.equals("")) {
                System.out.println("# Uncategorized");
            } else {
                System.out.println("# " + row);
            }

            // Print the single-key bindings
            for (Entry<Input, String> entry : keyTable.row(row).entrySet()) {
                System.out.println("`" + entry.getKey().getDisplayName() + "` : " + entry.getValue());
            }

            // Print the multi-key bindings
            for (String multiKey : multiKeys.get(row)) {
                System.out.println(multiKey);
            }
        }
    }
}

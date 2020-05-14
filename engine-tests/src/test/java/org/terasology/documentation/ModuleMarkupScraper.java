/*
 * Copyright 2020 MovingBlocks
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.terasology.config.Config;
import org.terasology.config.SelectModulesConfig;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.i18n.TranslationSystem;
import org.terasology.input.*;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.UniverseWrapper;
import org.terasology.rendering.nui.widgets.ResettableUIText;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.BlockLifecycleEvent;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.lang.annotation.Annotation;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class ModuleMarkupScraper {


    private ModuleMarkupScraper() {
        // Utility class, no instances
    }

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        Path homePath = Paths.get("");
        PathManager.getInstance().useOverrideHomePath(homePath);
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleEnvironment environment = moduleManager.getEnvironment();

//        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
//            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
//            if (!latestVersion.isOnClasspath()) {
//
//                System.out.println(latestVersion.getMetadata().getId());
//
//
//
//
////                ModuleSelectionInfo info = ModuleSelectionInfo.local(latestVersion);
////                modulesLookup.put(info.getMetadata().getId(), info);
////                sortedModules.add(info);
//            }
//        }

//        Map<String, InputCategory> inputCategories = Maps.newHashMap();
//        Map<SimpleUri, RegisterBindButton> inputsById = Maps.newHashMap();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {

//            if(moduleId.toString().equals("engine") || moduleId.toString().equals("unittest") )
//                continue;

            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                System.out.println("Examining Module: " + moduleId);

//                if(!moduleId.toString().equals("BiomesAPI"))
//                    continue;

                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment2 = moduleManager.loadEnvironment(result.getModules(), false)) {
//                        for (Class<? extends ComponentSystem> componentType : environment2.getSubtypesOf(ComponentSystem.class)) {
//                            Name mod = environment2.getModuleProviding(componentType);
//
//                            if(!mod.toString().equals(moduleId.toString()))
//                                continue;
//                            //String componentName = MetadataUtil.getComponentClassName(componentType);
//                            System.out.println("  Component: " + componentType.getName() + " | From Module: " + mod);
//                        }

                        for (Class<? extends Event> type : environment2.getSubtypesOf(Event.class)) {

                            Name mod = environment2.getModuleProviding(type);

                            if(!mod.toString().equals(moduleId.toString()))
                                continue;

                            System.out.println("  Event: " + type.getSimpleName() + " | From Module: " + mod);

                            // Are there any other classes derived from this class?
                            for (Class<? extends Object> eventType : environment2.getSubtypesOf(type)) {
                                System.out.println("    " + eventType.getName() + " | Event Module: " + environment2.getModuleProviding(eventType));
                            }
                        }

                    }
                }

            }
        }

//        for (Class<? extends Component> componentType : environment.getSubtypesOf(Component.class)) {
//            if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
//                String componentName = MetadataUtil.getComponentClassName(componentType);
//                System.out.println("Component: " + componentName + " | From Module: " + environment.getModuleProviding(componentType));
//            }
//        }
//
//
//
//        for (Class<? extends Event> type : environment.getSubtypesOf(Event.class)) {
//            //if (type.getAnnotation(DoNotAutoRegister.class) == null) {
//                System.out.println("Event: " + type.getSimpleName() + " | From Module: " + environment.getModuleProviding(type));
//
//
//            for (Class<? extends Object> eventType : environment.getSubtypesOf(type)) {
//                //if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
////                String componentName = MetadataUtil.getComponentClassName(eventType);
//                System.out.println("    " + eventType.getName() + " | Event Module: " + environment.getModuleProviding(eventType));
//                //}
//            }
//            //}
//        }

//        for (Class<? extends BaseComponentSystem> type : moduleManager.getEnvironment().getSubtypesOf(BaseComponentSystem.class)) {
//
//            System.out.println(type.toString());
//
//            Annotation[] annotations = type.getAnnotations();
//            for (Annotation annotation : annotations) {
//                System.out.println(annotation.toString());
//            }
//
//
//
//        }


//
//
//        // Holds normal input mappings where there is only one key
//        Multimap<InputCategory, String> categories = ArrayListMultimap.create();
//        Multimap<String, Input> keys = ArrayListMultimap.create();
//        Map<String, String> desc = new HashMap<>();
//
//        for (Class<?> holdingType : moduleManager.getEnvironment().getTypesAnnotatedWith(InputCategory.class)) {
//            InputCategory inputCategory = holdingType.getAnnotation(InputCategory.class);
//            categories.put(inputCategory, null);
//            for (String button : inputCategory.ordering()) {
//                categories.put(inputCategory, button);
//            }
//        }
//
//        for (Class<?> buttonEvent : moduleManager.getEnvironment().getTypesAnnotatedWith(RegisterBindButton.class)) {
//            DefaultBinding defBinding = buttonEvent.getAnnotation(DefaultBinding.class);
//            RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
//
//            String cat = info.category();
//            String id = "engine:" + info.id();
//            desc.put(id, info.description());
//
//            if (cat.isEmpty()) {
//                InputCategory inputCategory = findEntry(categories, id);
//                if (inputCategory == null) {
//                    System.out.println("Invalid category for: " + info.id());
//                }
//            } else {
//                InputCategory inputCategory = findCategory(categories, cat);
//                if (inputCategory != null) {
//                    categories.put(inputCategory, id);
//                } else {
//                    System.out.println("Invalid category for: " + info.id());
//                }
//            }
//
//            if (defBinding != null) {
//                // This handles bindings with just one key
//                Input input = defBinding.type().getInput(defBinding.id());
//                keys.put(id, input);
//            } else {
//                // See if there is a multi-mapping for this button
//                DefaultBindings multiBinding = buttonEvent.getAnnotation(DefaultBindings.class);
//
//                // Annotation math magic. We're expecting a DefaultBindings containing one DefaultBinding pair
//                if (multiBinding != null && multiBinding.value().length == 2) {
//                    DefaultBinding[] bindings = multiBinding.value();
//                    Input primary = bindings[0].type().getInput(bindings[0].id());
//                    Input secondary = bindings[1].type().getInput(bindings[1].id());
//                    keys.put(id, primary);
//                    keys.put(id, secondary);
//                }
//            }
//        }
//
//        for (InputCategory row : categories.keySet()) {
//            System.out.println("# " + row.displayName());
//
//            categories.get(row).stream().filter(entry -> entry != null).forEach(entry ->
//                    System.out.println(desc.get(entry) + ": " + keys.get(entry)));
//        }
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


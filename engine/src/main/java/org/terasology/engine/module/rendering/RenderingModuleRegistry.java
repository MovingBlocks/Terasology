/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.engine.module.rendering;

import org.terasology.context.Context;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.rendering.dag.ModuleRendering;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@API
public class RenderingModuleRegistry {

    private List<ModuleRendering> orderedModuleRenderingInstances = new ArrayList<>();

    public RenderingModuleRegistry() {

    }

    /**
     * Return {@link ModuleRendering} instance by classtype
     * @param moduleRenderingClass Class extending {@link ModuleRendering}
     * @return
     */
    @Nullable
    public ModuleRendering getModuleRenderingByClass(Class<? extends ModuleRendering> moduleRenderingClass) {
        // TODO HashMap for storing
        ModuleRendering moduleRenderingInstance = null;
        for (ModuleRendering moduleRendering : orderedModuleRenderingInstances) {
            if (moduleRendering.getClass().equals(moduleRenderingClass)) {
                moduleRenderingInstance = moduleRendering;
            }
        }
        return moduleRenderingInstance;
    }

    public ModuleRendering getModuleRenderingByModuleId(Name renderingModuleId) {
        // TODO HashMap for storing
        ModuleRendering moduleRenderingInstance = null;
        for (ModuleRendering moduleRendering : orderedModuleRenderingInstances) {
            if (moduleRendering.getProvidingModule().equals(renderingModuleId)) {
                moduleRenderingInstance = moduleRendering;
            }
        }
        return moduleRenderingInstance;
    }

    /**
     * For use everywhere to get last ordered list of {@link ModuleRendering} instances.
     * @return list of {@link ModuleRendering} instances
     */
    public List<ModuleRendering> getOrderedRenderingModules() {
        return this.orderedModuleRenderingInstances;
    }

    /**
     * Fetches list of {@link ModuleRendering} instances from given {@link ModuleEnvironment} and returns ordered
     * list based on their {@code ModuleRendering.initializationPriority} attribute.
     * @param moduleEnvironment Current module environment
     * @param context Current context
     * @return list of ordered {@link ModuleRendering} instances
     */
    public List<ModuleRendering> updateRenderingModulesOrder(ModuleEnvironment moduleEnvironment, Context context) {
        return calculateModuleOrder(fetchRenderingModules(moduleEnvironment, context));
    }

    /**
     * Sets new context for each ModuleRendering instance
     * @param context
     */
    public void updateModulesContext(Context context) {
        for (ModuleRendering moduleRendering : orderedModuleRenderingInstances) {
            moduleRendering.setContext(context);
            // context.put(ModuleRendering.class, moduleRendering);
        }
    }

    public void setRenderingModulePriority(ModuleRendering module, int initializationPriority) {
        module.setInitPriority(initializationPriority);
    }

    public int getRenderingModulePriority(ModuleRendering module) {
        return module.getInitPriority();
    }

    public Name getRenderingModuleId(ModuleRendering module) {
        return module.getProvidingModule();
    }

    /**
     * Sorts given list of {@link ModuleRendering} instances by {@code ModuleRendering.initializationPriority} attribute, saves and returns it.
     * @param activeModuleRenderingInstances List of {@link ModuleRendering} instances
     * @return Ordered list of {@link ModuleRendering} instances
     */
    private List<ModuleRendering> calculateModuleOrder(Set<ModuleRendering> activeModuleRenderingInstances) {
        orderedModuleRenderingInstances = activeModuleRenderingInstances.stream()
                .sorted(Comparator.comparing(ModuleRendering::getInitPriority)) // sort ascending by initializationPriority attribute
                .collect(Collectors.toList()); //convert stream to List again
        return orderedModuleRenderingInstances;
    }

    /**
     * Fetches all loaded {@link ModuleRendering} classes from the given {@link ModuleEnvironment}
     * and instantiatest them with current context.
     * @param moduleEnvironment
     * @param context
     * @return list of {@link ModuleRendering} instances
     */
    private Set<ModuleRendering> fetchRenderingModules(ModuleEnvironment moduleEnvironment, Context context) {
        Set<ModuleRendering> moduleSet = new HashSet<>();

        for (Class<? extends ModuleRendering> renderingClass : moduleEnvironment.getSubtypesOf(ModuleRendering.class)) {
            ModuleRendering moduleRenderingInstance = getModuleRenderingByClass(renderingClass);
            if (moduleRenderingInstance == null) {
                try {
                    Constructor<?> constructor = renderingClass.getConstructor(Context.class);
                    moduleRenderingInstance = (ModuleRendering) constructor.newInstance(context);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (moduleRenderingInstance != null) {
                moduleSet.add(moduleRenderingInstance);
            }
        }
        return moduleSet;
    }
}

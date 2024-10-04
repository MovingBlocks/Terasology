// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module.rendering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.annotation.API;
import org.terasology.engine.context.Context;
import org.terasology.engine.rendering.dag.ModuleRendering;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderingModuleRegistry.class);

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
                    LOGGER.error("Couldn't get constructor: ", e);
                }
            }
            if (moduleRenderingInstance != null) {
                moduleSet.add(moduleRenderingInstance);
            }
        }
        return moduleSet;
    }
}

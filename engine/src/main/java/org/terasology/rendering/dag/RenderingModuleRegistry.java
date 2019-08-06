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
package org.terasology.rendering.dag;

import org.terasology.context.Context;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.rendering.dag.gsoc.ModuleRendering;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RenderingModuleRegistry {

    // TODO need this? not implemented
    private List<Module> activeRenderingModules;

    private List<ModuleRendering> orderedModuleRenderingInstances = new ArrayList<>();

    public RenderingModuleRegistry() {

    }

    /**
     * For use everywhere to get last order
     * @return
     */
    public List<ModuleRendering> getOrderedRenderingModules() {
        return this.orderedModuleRenderingInstances;
    }

    
    public List<ModuleRendering> updateRenderingModulesOrder(ModuleEnvironment moduleEnvironment, Context preloadedSubContext) {
        this.orderedModuleRenderingInstances = calculateModuleOrder(fetchRenderingModules(moduleEnvironment, preloadedSubContext));
        return this.orderedModuleRenderingInstances;
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

    private List<ModuleRendering> calculateModuleOrder(List<ModuleRendering> activeModuleRenderingInstances) {
        // Sorted copy of activeRenderingModules
        orderedModuleRenderingInstances = activeModuleRenderingInstances.stream()
                .sorted(Comparator.comparing(ModuleRendering::getInitPriority)) // sort ascending by initializationPriority attribute
                .collect(Collectors.toList()); //convert stream to List again
        return orderedModuleRenderingInstances;
    }

    // TODO the getSubtypesOf approach might in the future be changed for Annotations @RenderingModule when the ModuleRendering.java
    // TODO gets deprecated by being merged into module management itself. Another option is for renderingModules to register themselves in the
    // TODO module management (at this stage equivalently register themselves in the ModuleRendering upon instantiaion (pre ECS system init) )
    private List<ModuleRendering> fetchRenderingModules(ModuleEnvironment moduleEnvironment, Context preloadedSubContext) {
        List<ModuleRendering> moduleList = new ArrayList<>();

        for (Class<? extends ModuleRendering> renderingClass : moduleEnvironment.getSubtypesOf(ModuleRendering.class)) {
            if (!orderedModuleRenderingInstances.contains(renderingClass)) {
                ModuleRendering moduleRendering = null;
                try {
                    Constructor<?> constructor = renderingClass.getConstructor(Context.class);
                    moduleRendering = (ModuleRendering) constructor.newInstance(preloadedSubContext);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (moduleRendering != null) {
                    moduleList.add(moduleRendering);
                }
            }
        }
        return moduleList;
    }
}

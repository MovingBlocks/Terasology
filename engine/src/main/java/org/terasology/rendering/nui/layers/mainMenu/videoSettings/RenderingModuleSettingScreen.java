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
package org.terasology.rendering.nui.layers.mainMenu.videoSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.dag.gsoc.ModuleRendering;
import org.terasology.rendering.nui.CoreScreenLayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RenderingModuleSettingScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:renderingModuleSettingScreen");

    private static final Logger logger = LoggerFactory.getLogger(RenderingModuleSettingScreen.class);

    private List<ModuleRendering> renderingModules = new ArrayList<>();
    private List<ModuleRendering> orderedRenderingModules = new ArrayList<>();

    @In
    private Context context;
    private ModuleManager moduleManager;

    public RenderingModuleSettingScreen() {

    }

    @Override
    public void onOpened() {
        renderingModules = fetchRenderingModules();
        orderedRenderingModules = calculateModuleOrder(renderingModules);
    }

    @Override
    public void initialise() {
        moduleManager = context.get(ModuleManager.class);
    }

    public List<ModuleRendering> getRenderingModules() {
        return renderingModules;
    }

    public List<ModuleRendering> getOrderedRenderingModules() {
        return orderedRenderingModules;
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

    private List<ModuleRendering> calculateModuleOrder(List<ModuleRendering> activeRenderingModules) {
        // Sorted copy of activeRenderingModules
        orderedRenderingModules = activeRenderingModules.stream()
                .sorted(Comparator.comparing(ModuleRendering::getInitPriority)) // sort ascending by initializationPriority attribute
                .collect(Collectors.toList()); //convert stream to List again
        return orderedRenderingModules;
    }

    // TODO the getSubtypesOf approach might in the future be changed for Annotations @RenderingModule when the ModuleRendering.java
    // TODO gets deprecated by being merged into module management itself. Another option is for renderingModules to register themselves in the
    // TODO module management (at this stage equivalently register themselves in the ModuleRendering upon instantiaion (pre ECS system init) )
    private List<ModuleRendering> fetchRenderingModules() {
            List<ModuleRendering> renderingModuleBaseClasses = new ArrayList<>();
        moduleManager.getEnvironment().getSubtypesOf(ModuleRendering.class)
                .forEach((renderingClass)->renderingModuleBaseClasses.add(context.get(renderingClass)));
        return renderingModuleBaseClasses;
    }
}

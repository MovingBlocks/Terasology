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
import org.terasology.engine.module.RenderingModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.registry.In;
import org.terasology.rendering.dag.RenderingModuleRegistry;
import org.terasology.rendering.dag.gsoc.ModuleRendering;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.*;

public class RenderingModuleSettingScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:renderingModuleSettingScreen");

    private static final Logger logger = LoggerFactory.getLogger(RenderingModuleSettingScreen.class);

    private List<ModuleRendering> orderedModuleRenderingInstances = new ArrayList<>();

    @In
    private Context context;
    private Context subContext;
    private ModuleEnvironment moduleEnvironment;

    private RenderingModuleRegistry renderingModuleRegistry;

    private UIText renderingModuleInfo;
    private UIButton recalculateOrder;

    public RenderingModuleSettingScreen() {

    }

    @Override
    public void onOpened() {
        //This must be here not in constructor. subContext is set post-creation
        moduleEnvironment = subContext.get(ModuleEnvironment.class);

        renderingModuleRegistry = context.get(RenderingModuleManager.class).getRegistry();

        renderingModuleRegistry.updateRenderingModulesOrder(moduleEnvironment, subContext);
        orderedModuleRenderingInstances = renderingModuleRegistry.getOrderedRenderingModules();

        renderingModuleInfo = find("modulesInfo", UIText.class);
        recalculateOrder = find("update", UIButton.class);

        if (recalculateOrder != null) {
            StringBuilder infoText = new StringBuilder("");
            int[] idx = {1};
            orderedModuleRenderingInstances.forEach(
                    (module)-> infoText.append(String.format("%d. %s (Priority: %d)\n",
                                idx[0]++, module.getProvidingModule(), module.getInitPriority()))
            );
            renderingModuleInfo.setText(infoText.toString());

            recalculateOrder.subscribe(button -> {
                renderingModuleRegistry.updateRenderingModulesOrder(moduleEnvironment, subContext);
                orderedModuleRenderingInstances = renderingModuleRegistry.getOrderedRenderingModules();
                StringBuilder infoTextLambda = new StringBuilder("");
                idx[0] = 1;
                orderedModuleRenderingInstances.forEach(
                        (module)-> infoTextLambda.append(String.format("%d. %s (Priority: %d)\n",
                                    idx[0]++, module.getProvidingModule(), module.getInitPriority()))
                );
                renderingModuleInfo.setText(infoTextLambda.toString());
            });
        }

        WidgetUtil.trySubscribe(this, "return", button ->
                triggerBackAnimation());
    }

    @Override
    public void initialise() {

    }

    public void setSubContext(Context subContext) {
        this.subContext = subContext;
    }
}

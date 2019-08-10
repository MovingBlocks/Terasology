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
import org.terasology.engine.module.rendering.RenderingModuleManager;
import org.terasology.math.geom.Vector2i;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.engine.module.rendering.RenderingModuleRegistry;
import org.terasology.rendering.dag.gsoc.ModuleRendering;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.StartPlayingScreen;
import org.terasology.rendering.nui.widgets.*;

import java.util.*;

public class RenderingModuleSettingScreen extends CoreScreenLayer implements UISliderOnChangeTriggeredListener {
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
    private UISlider initPrioritySlider;
    private UIDropdownScrollable<ModuleRendering> moduleList;

    public RenderingModuleSettingScreen() {
//This must be here not in constructor. subContext is set post-creation

    }

    public void postInit() {
        moduleEnvironment = subContext.get(ModuleEnvironment.class);

        renderingModuleRegistry = context.get(RenderingModuleManager.class).getRegistry();

        renderingModuleRegistry.updateRenderingModulesOrder(moduleEnvironment, subContext);
        orderedModuleRenderingInstances = renderingModuleRegistry.getOrderedRenderingModules();

        renderingModuleInfo = find("modulesInfo", UIText.class);
        recalculateOrder = find("update", UIButton.class);

        // List<Name> orderedModuleNames = new ArrayList<>();
        // orderedModuleRenderingInstances.forEach(module->orderedModuleNames.add(module.getProvidingModule()));

        initPrioritySlider = find("moduleInitPrioritySlider", UISlider.class);
        if (initPrioritySlider != null) {
            initPrioritySlider.setValue(2f);
            initPrioritySlider.setUiSliderOnChangeTriggeredListener(this);
        }

        moduleList = find("moduleNameList", UIDropdownScrollable.class);
        if (moduleList != null) {
            moduleList.bindSelection(new Binding<ModuleRendering>() {
                ModuleRendering selected;
                @Override
                public ModuleRendering get() {
                    return selected;
                }

                @Override
                public void set(ModuleRendering value) {
                    if (initPrioritySlider != null) {
                        initPrioritySlider.setValue(value.getInitPriority());
                    }
                    selected = value;
                }
            });

            moduleList.setOptions(orderedModuleRenderingInstances);
            moduleList.setVisibleOptions(5);
            moduleList.setSelection(orderedModuleRenderingInstances.get(0));
            initPrioritySlider.setValue(moduleList.getSelection().getInitPriority());
            moduleList.setOptionRenderer(new StringTextRenderer<ModuleRendering>() {
                @Override
                public String getString(ModuleRendering value) {
                    if (value != null) {
                        StringBuilder stringBuilder = new StringBuilder()
                                .append(String.format("%s",value.getClass().getSimpleName(),
                                value.getProvidingModule()));
                        return stringBuilder.toString();
                    }
                    return "";
                }
                @Override
                public void draw(ModuleRendering value, Canvas canvas) {
                    canvas.drawText(getString(value), canvas.getRegion());
                }
            });
        }

        if (recalculateOrder != null) {
            updateRenderingModuleInfo();
            recalculateOrder.subscribe(button -> {
                renderingModuleRegistry.updateRenderingModulesOrder(moduleEnvironment, subContext);
                orderedModuleRenderingInstances = renderingModuleRegistry.getOrderedRenderingModules();
                StringBuilder infoTextLambda = new StringBuilder("");
                int[] idx = {1};
                orderedModuleRenderingInstances.forEach(
                        (module)-> infoTextLambda.append(String.format("%d. %s (Priority: %d)\n",
                                idx[0]++, module.getProvidingModule(), module.getInitPriority()))
                );
                renderingModuleInfo.setText(infoTextLambda.toString());
            });
        }

        // TODO returns one more screen every time...gradually
        WidgetUtil.trySubscribe(this, "return", widget ->
                getManager().pushScreen(StartPlayingScreen.ASSET_URI));

        // Update slider if module selection changes
//        if (initPrioritySlider != null && moduleList != null) {
//            moduleList. (this, "moduleNameList", widget->initPrioritySlider.setValue(moduleList.getSelection().getInitPriority()));
//        }
    }

    @Override
    public void onOpened() {
        super.onOpened();

    }

    private void updateRenderingModuleInfo() {
        StringBuilder infoText = new StringBuilder("");
        int[] idx = {1};
        orderedModuleRenderingInstances.forEach(
                (module)-> infoText.append(String.format("%d. %s - in %s module (Priority: %d)\n",
                        idx[0]++, module.getClass().getSimpleName(), module.getProvidingModule(), module.getInitPriority()))
        );
        renderingModuleInfo.setText(infoText.toString());
    }

    @Override
    public void onSliderValueChanged(float val) {
        moduleList.getSelection().setInitPriority((int) val);
        updateRenderingModuleInfo();
    }

    @Override
    public void initialise() {

    }

    public void setSubContext(Context subContext) {
        this.subContext = subContext;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.module.rendering.RenderingModuleRegistry;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.nui.Canvas;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UISlider;
import org.terasology.nui.widgets.UISliderOnChangeTriggeredListener;
import org.terasology.nui.widgets.UIText;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.dag.ModuleRendering;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.layers.mainMenu.StartPlayingScreen;

import java.util.ArrayList;
import java.util.List;

public class RenderingModuleSettingScreen extends CoreScreenLayer implements UISliderOnChangeTriggeredListener {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:renderingModuleSettingScreen");

    private static final Logger logger = LoggerFactory.getLogger(RenderingModuleSettingScreen.class);

    private List<ModuleRendering> orderedModuleRenderingInstances = new ArrayList<>();

    @In
    private Context context;
    private Context subContext;
    private ModuleEnvironment moduleEnvironment;

    @In
    private TranslationSystem translationSystem;

    private RenderingModuleRegistry renderingModuleRegistry;

    private UIText renderingModuleInfo;
    private UIButton recalculateOrder;
    private UISlider initPrioritySlider;
    private UIDropdownScrollable<ModuleRendering> moduleList;
    private UIButton setEnabledRenderingClassButton;

    public RenderingModuleSettingScreen() {
//This must be here not in constructor. subContext is set post-creation

    }

    public void postInit() {
        moduleEnvironment = subContext.get(ModuleEnvironment.class);

        renderingModuleRegistry = context.get(RenderingModuleRegistry.class);

        orderedModuleRenderingInstances = renderingModuleRegistry.updateRenderingModulesOrder(moduleEnvironment, subContext);

        if (orderedModuleRenderingInstances.isEmpty()) {
            logger.error("No rendering module found!");
            GameEngine gameEngine = context.get(GameEngine.class);
            gameEngine.changeState(new StateMainMenu("No rendering module installed, unable to render. Try enabling CoreRendering."));
            return;
        }

        renderingModuleInfo = find("modulesInfo", UIText.class);
        recalculateOrder = find("update", UIButton.class);
        setEnabledRenderingClassButton = find("setEnabledRenderingClassButton", UIButton.class);

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
            if (initPrioritySlider != null) {
                initPrioritySlider.setValue(moduleList.getSelection().getInitPriority());
            }
            moduleList.setOptionRenderer(new StringTextRenderer<ModuleRendering>() {
                @Override
                public String getString(ModuleRendering value) {
                    if (value != null) {
                        StringBuilder stringBuilder = new StringBuilder()
                                .append(String.format("%s", value.getClass().getSimpleName()));
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
                updateRenderingModuleInfo();
            });
        }

        if (setEnabledRenderingClassButton != null && moduleList != null) {
            setEnabledRenderingClassButton.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return (moduleList.getSelection().isEnabled())
                            ? translationSystem.translate("${engine:menu#disable-rendering-class}")
                            : translationSystem.translate("${engine:menu#enable-rendering-class}");
                }
            });

            setEnabledRenderingClassButton.subscribe(button -> {
                moduleList.getSelection().toggleEnabled();
                updateRenderingModuleInfo();
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
        StringBuilder infoText = new StringBuilder("Rendering Modules\n").append("=================\n");
        int[] idx = {1};
        if (!orderedModuleRenderingInstances.isEmpty()) {
            orderedModuleRenderingInstances.forEach(
                    (module) -> infoText.append(
                            String.format("%d. %s - in %s module (Priority: %d, Enabled: %s)\n",
                                    idx[0]++,
                                    module.getClass().getSimpleName(),
                                    module.getProvidingModule(),
                                    module.getInitPriority(),
                                    String.valueOf(module.isEnabled())
                            )
                    )
            );
        }
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

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i vector2i) {
        return vector2i;
    }
}

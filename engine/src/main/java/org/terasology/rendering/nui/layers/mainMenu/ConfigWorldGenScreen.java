/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.IntToFloatBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.params.BooleanParameter;
import org.terasology.world.generator.params.FloatParameter;
import org.terasology.world.generator.params.IntParameter;
import org.terasology.world.generator.params.Parameter;
import org.terasology.world.generator.params.StringParameter;

/**
 * @author Immortius
 */
public class ConfigWorldGenScreen extends UIScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWorldGenScreen.class);

    @In
    private ModuleManager moduleManager;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private Config config;

    private int imageSize = 128;

    private ScrollableArea list;

    private WorldConfigurator worldConfig;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });

        WorldGeneratorInfo info = worldGeneratorManager.getWorldGeneratorInfo(config.getWorldGeneration().getDefaultGenerator());
        Module worldGeneratorModule = moduleManager.getLatestModuleVersion(info.getUri().getModuleName());
        try {
            moduleManager.enableModuleAndDependencies(worldGeneratorModule);
            WorldGenerator wg = CoreRegistry.get(WorldGeneratorManager.class).createGenerator(info.getUri());

            if (wg.getConfigurator().isPresent()) {
                worldConfig = wg.getConfigurator().get();
            } else {
                logger.info(info.getUri().toString() + " does not support configuration");
                return;
            }
        } catch (UnresolvedWorldGeneratorException e) {
            // if errors happen, don't enable this feature
            logger.error("Unable to load world generator: " + info.getUri().toString());
        } finally {
            moduleManager.disableAllModules();
        }

        list = find("params", ScrollableArea.class);
        if (list != null) {
            ColumnLayout layout = new ColumnLayout();
            layout.setColumns(2);
            layout.setVerticalSpacing(4);
            layout.setHorizontalSpacing(8);
            layout.setFamily("option-grid");
            layout.setColumnWidths(0.25f, 0.75f);
            list.setContent(layout);

            for (Parameter p : worldConfig.getParams()) {
                UILabel label = new UILabel(p.getLabel());
                UIWidget widget = getWidgetFor(p);
                layout.addWidget(label, null);
                layout.addWidget(widget, null);
            }
        }


    }

    private UIWidget getWidgetFor(Parameter p) {
        if (p instanceof IntParameter) {
            final IntParameter ip = (IntParameter) p;
            UISlider slider = new UISlider();
            slider.setIncrement(1.0f);
            slider.setPrecision(0);
            slider.setMinimum(ip.getMin());
            slider.setRange(ip.getMax() - ip.getMin());
            Binding<Float> binding = new IntToFloatBinding(ip.getBinding());
            slider.bindValue(binding);
            return slider;
        }

        if (p instanceof FloatParameter) {
            final FloatParameter fp = (FloatParameter) p;
            UISlider slider = new UISlider();
            slider.setIncrement(fp.getStep());
            slider.setMinimum(fp.getMin());
            slider.setRange(fp.getMax() - fp.getMin());
            Binding<Float> binding = fp.getBinding();
            slider.bindValue(binding);
            return slider;
        }
        
        if (p instanceof StringParameter) {
            final StringParameter sp = (StringParameter) p;
            UIText text = new UIText();
            text.bindText(sp.getBinding());
            return text;
        }
        
        if (p instanceof BooleanParameter) {
            final BooleanParameter bp = (BooleanParameter) p;
            UICheckbox checkbox = new UICheckbox();
            checkbox.bindChecked(bp.getBinding());
            return checkbox;
        }
        
        return new UILabel("-");
    }
    
}



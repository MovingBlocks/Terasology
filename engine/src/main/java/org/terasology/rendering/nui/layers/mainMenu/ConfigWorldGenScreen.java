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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.layouts.PropertyLayout;
import org.terasology.rendering.nui.properties.PropertyOrdering;
import org.terasology.rendering.nui.properties.PropertyProvider;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;

/**
 * A config screen for world generation
 * @author Martin Steiger
 */
public class ConfigWorldGenScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(ConfigWorldGenScreen.class);

    @In
    private ModuleManager moduleManager;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private Config config;

    private int imageSize = 128;

    private WorldConfigurator worldConfig;

    private PropertyLayout properties;

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

        properties = find("properties", PropertyLayout.class);
        if (properties != null) {
            properties.setOrdering(PropertyOrdering.byLabel());
            Map<String, ?> props = worldConfig.getProperties();
            for (String label : props.keySet()) {
                PropertyProvider<?> provider = new PropertyProvider<>(props.get(label));
                properties.addPropertyProvider(label, provider);
            }
        }


    }
}



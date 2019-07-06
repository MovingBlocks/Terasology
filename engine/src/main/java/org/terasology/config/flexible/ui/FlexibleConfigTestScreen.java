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
package org.terasology.config.flexible.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.FlexibleConfig;
import org.terasology.config.flexible.FlexibleConfigManager;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.config.flexible.internal.FlexibleConfigManagerImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.Optional;

public class FlexibleConfigTestScreen extends CoreScreenLayer {
    private static final SimpleUri CONFIG_ID = new SimpleUri("engine:TestConfig");
    private static final SimpleUri SETTING_ID = new SimpleUri("engine:TestSetting");
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexibleConfigTestScreen.class);

    private FlexibleConfigManager flexibleConfigManager = new FlexibleConfigManagerImpl();
    private FlexibleConfig flexibleConfig;

    private SettingUIManager settingUIManager;

    private ColumnLayout mainContainer;

    @In
    ModuleManager moduleManager;

    @In
    AssetManager assetManager;

    @Override
    public void initialise() {
        setupTestConfig();

        settingUIManager = new SettingUIManager(moduleManager.getEnvironment());
        mainContainer = find("mainContainer", ColumnLayout.class);

        mainContainer.addWidget(buildFlexibleConfigWidget(flexibleConfig));
    }

    private UIWidget buildFlexibleConfigWidget(FlexibleConfig flexibleConfig) {
        ColumnLayout container = new ColumnLayout();
        container.setVerticalSpacing(8);

        UILabel description = new UILabel(flexibleConfig.getDescription());
        container.addWidget(description);

        for (Setting<?> setting : flexibleConfig.getSettings()) {
            Optional<SettingUIWidget<?>> widget = settingUIManager.getWidgetFor(setting);

            if (!widget.isPresent()) {
                LOGGER.error("Couldn't find a widget for the Setting {}", setting.getId());
                continue;
            }

            SettingUIWidget<?> settingWidget = widget.get();

            settingWidget.loadContents(assetManager);
            settingWidget.bindToSetting(setting);

            container.addWidget(settingWidget);
        }

        return container;
    }

    private void setupTestConfig() {
        flexibleConfigManager.addNewConfig(CONFIG_ID, "Test Config");

        flexibleConfig = flexibleConfigManager.getConfig(CONFIG_ID);

        flexibleConfig.newSetting(SETTING_ID, Integer.class)
            .setDefaultValue(0)
            .setConstraint(new NumberRangeConstraint<>(-5, 5, true, true))
            .setHumanReadableName("Integer Test Setting")
            .setDescription("Integer Test Setting with Number Range")
            .addToConfig();
    }
}

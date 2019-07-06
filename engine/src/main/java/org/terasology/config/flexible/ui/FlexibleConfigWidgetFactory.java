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
import org.terasology.config.flexible.Setting;
import org.terasology.engine.module.ModuleManager;
import org.terasology.rendering.nui.UILayout;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.Optional;

public class FlexibleConfigWidgetFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexibleConfigWidgetFactory.class);

    private final SettingWidgetFactory settingWidgetFactory;
    private final AssetManager assetManager;

    public FlexibleConfigWidgetFactory(ModuleManager moduleManager, AssetManager assetManager) {
        this.settingWidgetFactory = new SettingWidgetFactory(moduleManager.getEnvironment());
        this.assetManager = assetManager;
    }

    public UIWidget buildWidgetFor(FlexibleConfig flexibleConfig) {
        UILayout<?> container = buildContainer();

        UILabel description = new UILabel(flexibleConfig.getDescription());
        addWidgetToContainer(container, description);

        for (Setting<?> setting : flexibleConfig.getSettings()) {
            Optional<SettingUIWidget<?>> widget = settingWidgetFactory.createWidgetFor(setting);

            if (!widget.isPresent()) {
                LOGGER.error("Couldn't find a widget for the Setting {}", setting.getId());
                continue;
            }

            SettingUIWidget<?> settingWidget = widget.get();

            settingWidget.loadContents(assetManager);
            settingWidget.bindToSetting(setting);

            addWidgetToContainer(container, settingWidget);
        }

        return container;
    }

    private void addWidgetToContainer(UILayout<?> container, UIWidget widget) {
        container.addWidget(widget, null);
    }

    private UILayout<?> buildContainer() {
        ColumnLayout container = new ColumnLayout();

        container.setVerticalSpacing(8);

        return container;
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.AutoConfig;
import org.terasology.config.flexible.Setting;
import org.terasology.engine.module.ModuleManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.PropertyLayout;
import org.terasology.nui.properties.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class AutoConfigWidgetFactory {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfigWidgetFactory.class);

    private final SettingWidgetFactory settingWidgetFactory;
    private final AssetManager assetManager;

    public AutoConfigWidgetFactory(ModuleManager moduleManager, AssetManager assetManager) {
        this.settingWidgetFactory = new SettingWidgetFactory(moduleManager.getEnvironment(), assetManager);
        this.assetManager = assetManager;
    }

    public UIWidget buildWidgetFor(AutoConfig config) {
        PropertyLayout container = new PropertyLayout();
        container.setRowConstraints("[min]");

        Collection<Property<?, ?>> widgetProperties = new ArrayList<>();

        for (Setting<?> setting : config.getSettings()) {
            Optional<UIWidget> settingWidget = settingWidgetFactory.createWidgetFor(setting);

            if (!settingWidget.isPresent()) {
                logger.error("Couldn't find a widget for the Setting");
                continue;
            }

            widgetProperties.add(
                    new Property<>(setting.getHumanReadableName(),
                            null,
                            settingWidget.get(),
                            setting.getDescription())
            );
        }

        container.addProperties("config.getDescription()", widgetProperties);

        return container;
    }

}

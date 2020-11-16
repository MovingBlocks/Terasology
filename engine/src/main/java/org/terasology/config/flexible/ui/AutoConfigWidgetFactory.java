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
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.reflection.TypeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Creates {@link UIWidget} for {@link AutoConfig}.
 * <p>
 * Created {@link UIWidget}'s binded with source {@link AutoConfig}. Changing values in {@link UIWidget} will change
 * value in related {@link AutoConfig}
 * <p>
 * Using {@link SettingWidgetFactory} for creating widgets for individual {@link Setting}.
 * <p>
 * Useful for creating settings UI.
 * <p>
 * Usage:
 * <pre>
 * class SomeScreen extends CoreScreenLayer {
 *     /@Override
 *     public void initialise() {
 *         AutoConfigWidgetFactory configWidgetFactory = new AutoConfigWidgetFactory(moduleManager, assetManager);
 *         ColumnLayout mainContainer = find("mainContainer", ColumnLayout.class);
 *         mainContainer.addWidget(configWidgetFactory.buildWidgetFor(config));
 *     }
 * }
 * </pre>
 */
public class AutoConfigWidgetFactory implements TypeWidgetFactory {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfigWidgetFactory.class);

    private final SettingWidgetFactory settingWidgetFactory;
    private final AssetManager assetManager;

    public AutoConfigWidgetFactory(ModuleManager moduleManager, AssetManager assetManager) {
        this.settingWidgetFactory = new SettingWidgetFactory(moduleManager.getEnvironment(), assetManager);
        this.assetManager = assetManager;
    }

    /**
     * Creates {@link UIWidget} for {@link AutoConfig}
     *
     * @param config for creating widget
     * @return UIWidget created for config
     */
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

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        if (!AutoConfig.class.isAssignableFrom(type.getRawType())) {
            return Optional.empty();
        }
        return Optional.of(binding -> buildWidgetFor((AutoConfig) binding.get()));
    }

}

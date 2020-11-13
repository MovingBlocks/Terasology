// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIElement;
import org.terasology.registry.In;

import java.util.Optional;

public abstract class AssetBackedConstraintWidgetFactory<T, C extends SettingConstraint<T>>
    extends ConstraintWidgetFactory<T, C> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintWidgetFactory.class);
    private final String contentsUri;

    @In
    private AssetManager assetManager;

    protected AssetBackedConstraintWidgetFactory(String contentsUri) {
        this.contentsUri = contentsUri;
    }

    protected abstract void bindWidgetToSetting(UIWidget widget);

    @Override
    public Optional<UIWidget> buildWidget() {
        Optional<UIElement> uiElement = assetManager.getAsset(contentsUri, UIElement.class);

        if (!uiElement.isPresent()) {
            LOGGER.error("Can't find unique UI element '{}'", contentsUri);
            return Optional.empty();
        }
        uiElement = uiElement.get().createInstance();

        if (!uiElement.isPresent()) {
            LOGGER.error("Can't create copy of UI element '{}'", contentsUri);
            return Optional.empty();
        }

        UIWidget settingWidget = uiElement.get().getRootWidget();

        bindWidgetToSetting(settingWidget);

        return Optional.of(settingWidget);
    }
}

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
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.asset.UIElement;

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

        UIWidget settingWidget = uiElement.get().getRootWidget();

        bindWidgetToSetting(settingWidget);

        return Optional.of(settingWidget);
    }
}

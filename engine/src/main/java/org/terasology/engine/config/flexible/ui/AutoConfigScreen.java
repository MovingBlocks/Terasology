// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.config.flexible.AutoConfigManager;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

public class AutoConfigScreen extends CoreScreenLayer {
    public static final Logger logger = LoggerFactory.getLogger(AutoConfigScreen.class);
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:autoConfigScreen");

    @In
    private TypeWidgetLibrary typeWidgetLibrary;
    @In
    private ModuleManager moduleManager;
    @In
    private AssetManager assetManager;
    @In
    private AutoConfigManager configManager;

    private ColumnLayout mainContainer;

    @Override
    public void initialise() {
        mainContainer = find("mainContainer", ColumnLayout.class);
        assert mainContainer != null;
        for (AutoConfig config : configManager.getLoadedConfigs()) {
            Binding<AutoConfig> configBinding = new DefaultBinding<>(config);

            Optional<UIWidget> widget = typeWidgetLibrary.getWidget(configBinding, AutoConfig.class);
            if (widget.isPresent()) {
                mainContainer.addWidget(widget.get());
            } else {
                logger.warn("Cannot create widget for config: {}", config.getId()); //NOPMD
            }
        }
        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

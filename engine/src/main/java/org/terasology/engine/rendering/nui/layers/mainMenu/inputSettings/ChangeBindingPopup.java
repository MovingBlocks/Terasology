// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.inputSettings;

import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.BindsConfig;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.input.Input;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

import java.util.List;

public class ChangeBindingPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:changeBindingPopup");

    @In
    private Config config;

    @In
    private BindsManager bindsManager;

    @In
    private ModuleManager moduleManager;

    @In
    private TranslationSystem translationSystem;

    @In
    private Context context;

    private UIInputBind bindButton;

    private BindsConfig defaultBinds;
    private BindsConfig currBinds;

    @Override
    public void initialise() {
        defaultBinds = bindsManager.getDefaultBindsConfig();

        bindButton = find("new-binding", UIInputBind.class);
        WidgetUtil.trySubscribe(this, "remove", button -> bindButton.setNewInput(null));
        WidgetUtil.trySubscribe(this, "ok", button -> {
            Input newInput = bindButton.getNewInput();
            currBinds = bindsManager.getBindsConfig();
            if (currBinds.isBound(newInput) && !newInput.equals(bindButton.getInput())) {
                ConfirmChangePopup popup = getManager().pushScreen(ConfirmChangePopup.ASSET_URI, ConfirmChangePopup.class);
                popup.setButtonData(bindButton);
            } else {
                bindButton.saveInput();
                getManager().popScreen();
            }
        });
        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().popScreen());
    }

    public void setBindingData(SimpleUri uri, RegisterBindButton bind, int index) {
        find("title", UILabel.class).setText(translationSystem.translate(bind.description()));
        BindsConfig bindConfig = bindsManager.getBindsConfig();
        bindButton.bindInput(new InputConfigBinding(bindConfig, uri, index));
        List<Input> defaults = defaultBinds.getBinds(uri);
        find("default-binding", UILabel.class).setText(
                defaults.size() > index ? defaults.get(index).getDisplayName() : "<" + translationSystem.translate("${engine:menu#none}" + ">"));
        find("default", UIButton.class).subscribe(e -> bindButton.setNewInput(
                defaults.size() > index ? defaults.get(index) : null));
    }

    @Override
    public void onClosed() {
        super.onClosed();
        bindsManager.registerBinds();
    }
}

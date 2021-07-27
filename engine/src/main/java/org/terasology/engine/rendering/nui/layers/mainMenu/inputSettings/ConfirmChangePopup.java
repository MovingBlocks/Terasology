// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.inputSettings;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

public class ConfirmChangePopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:confirmChangePopup");

    @In
    private TranslationSystem translationSystem;

    private UIInputBind bindButton;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", button -> {
            bindButton.saveInput();
            getManager().popScreen();
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().popScreen());
    }

    public void setButtonData(UIInputBind button) {
        this.bindButton = button;
        String messageText = button.getNewInput().getDisplayName() + " " + translationSystem.translate("${engine:menu#change-keybind-popup-message}");
        find("message", UILabel.class).setText(messageText);
    }

}

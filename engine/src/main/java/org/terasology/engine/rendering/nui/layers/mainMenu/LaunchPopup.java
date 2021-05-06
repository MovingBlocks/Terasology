// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.BindHelper;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

/**
 * LaunchPopup will appear when game launches.
 */
public class LaunchPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:launchPopup!instance");

    @In
    private Config config;

    private Runnable yesHandler;

    private Runnable noHandler;

    private Runnable optionHandler;

    @Override
    public void initialise() {
        WidgetUtil.tryBindCheckbox(this, "showLaunchPopup", BindHelper.bindBeanProperty("launchPopupDisabled", config.getTelemetryConfig(), Boolean.TYPE));

        WidgetUtil.trySubscribe(this, "yes", (button) -> {
            yesHandler.run();
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "no", (button) -> {
            noHandler.run();
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "option", (button) -> {
            getManager().popScreen();
            optionHandler.run();
        });
    }

    public void setMessage(String title, String message) {
        UILabel titleLabel = find("title", UILabel.class);
        if (titleLabel != null) {
            titleLabel.setText(title);
        }

        UILabel messageLabel = find("message", UILabel.class);
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    /**
     * @param runnable will be called when the user clicks yes
     */
    public void setYesHandler(Runnable runnable) {
        this.yesHandler = runnable;
    }

    /**
     * @param runnable will be called when the user clicks no
     */
    public void setNoHandler(Runnable runnable) {
        this.noHandler = runnable;
    }

    public void setOptionHandler(Runnable runnable) {
        this.optionHandler = runnable;
    }

    public void setOptionButtonText(String buttonText) {
        UIButton optionButton = find("option", UIButton.class);
        optionButton.setText(buttonText);
    }
}

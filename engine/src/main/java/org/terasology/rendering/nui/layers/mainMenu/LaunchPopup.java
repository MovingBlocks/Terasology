/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.BindHelper;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;

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

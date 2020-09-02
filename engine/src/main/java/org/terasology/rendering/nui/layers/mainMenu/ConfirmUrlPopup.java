/*
 * Copyright 2018 MovingBlocks
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

import java.net.MalformedURLException;
import java.net.URL;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.WebBrowserConfig;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UICheckbox;
import org.terasology.nui.widgets.UILabel;

public class ConfirmUrlPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:confirmUrlPopup!instance");

    private Runnable leftActon;
    private Runnable rightAction;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "leftButton", button -> buttonCallback(leftActon));
        WidgetUtil.trySubscribe(this, "rightButton", button -> buttonCallback(rightAction));
    }

    private void buttonCallback(Runnable action) {
        getManager().popScreen();
        action.run();
    }

    public void setLeftButton(String text, Runnable action) {
        UIButton leftButton = find("leftButton", UIButton.class);
        if (leftButton != null) {
            leftButton.setText(text);
        }

        leftActon = action;
    }

    public void setRightButton(String text, Runnable action) {
        UIButton rightButton = find("rightButton", UIButton.class);
        if (rightButton != null) {
            rightButton.setText(text);
        }

        rightAction = action;
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

    public void setCheckbox(WebBrowserConfig webBrowserConfig, String url) throws MalformedURLException {
        UICheckbox saveHostName = find("saveHostName", UICheckbox.class);

        if (saveHostName != null) {
            saveHostName.setChecked(false);
            String hostName = new URL(url).getHost();

            saveHostName.subscribe(checkbox -> {
                boolean isTrustedHostName = saveHostName.isChecked();

                if (isTrustedHostName) {
                    webBrowserConfig.addTrustedHostName(hostName);
                } else {
                    webBrowserConfig.removeTrustedHostName(hostName);
                }
            });
        }

        UICheckbox saveUrl = find("saveUrl", UICheckbox.class);

        if (saveUrl != null) {
            saveUrl.setChecked(false);

            saveUrl.subscribe(checkbox -> {
                boolean isTrustedUrl = saveUrl.isChecked();

                if (isTrustedUrl) {
                    webBrowserConfig.addTrustedUrls(url);
                } else {
                    webBrowserConfig.removeTrustedUrl(url);
                }
            });
        }
    }
}

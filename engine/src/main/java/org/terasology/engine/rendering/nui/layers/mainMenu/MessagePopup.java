// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.context.annotation.API;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

@API
public class MessagePopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:messagePopup!instance");

    private final ActivateEventListener defaultCloseAction = button -> getManager().popScreen();

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", defaultCloseAction);
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

    public void subscribeButton(ActivateEventListener eventListener) {
        if (eventListener != null) {
            WidgetUtil.trySubscribe(this, "ok", eventListener);
        }
    }
}

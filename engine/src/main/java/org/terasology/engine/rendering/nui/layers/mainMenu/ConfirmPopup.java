// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

/**
 * Ask the user to confirm or cancel an action.
 */
public class ConfirmPopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:confirmPopup!instance");

    private Runnable okHandler;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", (button) -> {
            getManager().popScreen();
            okHandler.run();
        });
        WidgetUtil.trySubscribe(this, "cancel", (button) -> getManager().popScreen());
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
     * @param runnable will be called when the user clicks okay
     */
    public void setOkHandler(Runnable runnable) {
        this.okHandler = runnable;
    }
}

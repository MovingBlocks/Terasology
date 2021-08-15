// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

public class TwoButtonPopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:twoButtonPopup!instance");

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
        find("leftButton", UIButton.class).setText(text);
        leftActon = action;
    }

    public void setRightButton(String text, Runnable action) {
        find("rightButton", UIButton.class).setText(text);
        rightAction = action;
    }

    public void setMessage(String title, String message) {
        find("title", UILabel.class).setText(title);
        find("message", UILabel.class).setText(message);
    }

}

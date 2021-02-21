// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.rendering.nui.CoreScreenLayer;

/**
 * Ask the user to confirm or cancel an action.
 */
public class ThreeButtonPopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:threeButtonPopup!instance");

    private Runnable leftActon;
    private Runnable centerAction;
    private Runnable rightAction;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "leftButton", button -> buttonCallback(leftActon));
        WidgetUtil.trySubscribe(this, "centerButton", button -> buttonCallback(centerAction));
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

    public void setCenterButton(String text, Runnable action) {
        find("centerButton", UIButton.class).setText(text);
        centerAction = action;
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

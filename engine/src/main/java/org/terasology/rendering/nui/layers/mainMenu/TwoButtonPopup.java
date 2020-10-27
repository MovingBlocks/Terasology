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
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;

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

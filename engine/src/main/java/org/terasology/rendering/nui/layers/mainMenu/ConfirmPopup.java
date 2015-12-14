/*
 * Copyright 2015 MovingBlocks
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
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UILabel;

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

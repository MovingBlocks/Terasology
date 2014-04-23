/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * @author Immortius
 */
public class MessagePopup extends CoreScreenLayer {

    public static final AssetUri ASSET_URI = new AssetUri(AssetType.UI_ELEMENT, "engine:messagePopup");
    
    public final ActivateEventListener defaultCloseAction = new ActivateEventListener() {
        @Override
        public void onActivated(UIWidget button) {
            getManager().popScreen();
        }
    };

    public ActivateEventListener prevCloseAction;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", defaultCloseAction);
        prevCloseAction = defaultCloseAction;
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

    public void setCloseButtonText(String text) {
        UIButton button = find("ok", UIButton.class);
        if (button != null) {
            button.setText(text);
        }
    }

    /**
     * @param closeAction the event listener will be run instead of the default one (close the popup)
     */
    public void setCloseAction(ActivateEventListener closeAction) {
        UIButton button = find("ok", UIButton.class);
        if (button != null) {
            button.unsubscribe(prevCloseAction);
            button.subscribe(closeAction);
            prevCloseAction = closeAction;
        }
    }
    
    @Override
    public void onClosed() {
        super.onClosed();

        // don't save this asset in the cache -> don't persist changes to this class
        Assets.dispose(Assets.get(MessagePopup.ASSET_URI));
    }
}

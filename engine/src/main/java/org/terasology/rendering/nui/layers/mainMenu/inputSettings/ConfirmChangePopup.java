/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.inputSettings;

import org.terasology.assets.ResourceUrn;
import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UILabel;

public class ConfirmChangePopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:confirmChangePopup");

    @In
    private TranslationSystem translationSystem;

    private UIInputBind bindButton;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", button -> {
            bindButton.saveInput();
            getManager().popScreen();
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().popScreen());
    }

    public void setButtonData(UIInputBind button) {
        this.bindButton = button;
        String messageText = button.getNewInput().getDisplayName() + " " + translationSystem.translate("${engine:menu#change-keybind-popup-message}");
        find("message", UILabel.class).setText(messageText);
    }

}

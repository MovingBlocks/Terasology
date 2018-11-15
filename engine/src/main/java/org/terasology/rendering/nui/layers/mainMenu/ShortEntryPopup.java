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

import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.*;

public class ShortEntryPopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:shortEntryPopup!instance");

    private Binding<String> inputBinding;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "enterButton", button -> enterPressed());
        WidgetUtil.trySubscribe(this, "returnButton", button -> returnPressed());
    }

    private void enterPressed() {
        if (inputBinding != null) {
            inputBinding.set(find("nameInput", UIText.class).getText());
        }
        getManager().popScreen();
    }

    private void returnPressed() {
        getManager().popScreen();
    }

    public void setTitle(String title) {
        find("title", UILabel.class).setText(title);
    }

    public void setMessage(String message) {
        find("message", UILabel.class).setText(message);
    }

    public void bindInput(Binding<String> binding) {
        this.inputBinding = binding;
    }
}

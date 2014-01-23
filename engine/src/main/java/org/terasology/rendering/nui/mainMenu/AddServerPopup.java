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
package org.terasology.rendering.nui.mainMenu;

import org.terasology.config.Config;
import org.terasology.config.ServerInfo;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIText;

/**
 * @author Immortius
 */
public class AddServerPopup extends UIScreenLayer {

    @In
    private Config config;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {

                UIText name = find("name", UIText.class);
                UIText address = find("address", UIText.class);
                if (name != null && address != null) {
                    ServerInfo result = new ServerInfo(name.getText(), address.getText());
                    config.getNetwork().add(result);
                }
                getManager().popScreen();
            }
        });

        WidgetUtil.trySubscribe(this, "cancel", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
    }

    @Override
    public boolean isLowerLayerVisible() {
        return true;
    }
}

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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UIText;

public class EnterUsernamePopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:enterUsernamePopup");

    @In
    private Config config;

    private UIText username;
    private PlayerConfig playerConfig;

    @Override
    public void initialise() {
        playerConfig = config.getPlayer();

        username = find("username", UIText.class);
        username.setText(playerConfig.getName());

        WidgetUtil.trySubscribe(this, "ok", button -> {
            if (username != null && !username.getText().isEmpty()) {
                playerConfig.setName(username.getText());
                playerConfig.setHasEnteredUsername(true);
                getManager().popScreen();
            }
        });

        WidgetUtil.trySubscribe(this, "cancel", button -> {
            playerConfig.setHasEnteredUsername(true);
            getManager().popScreen();
        });
    }
}

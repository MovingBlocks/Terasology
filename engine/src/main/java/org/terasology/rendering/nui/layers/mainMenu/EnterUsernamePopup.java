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

import com.google.common.base.Strings;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIText;

public class EnterUsernamePopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:enterUsernamePopup");

    @In
    private Config config;
    @In
    private TranslationSystem translationSystem;

    private UIText username;
    private PlayerConfig playerConfig;

    @Override
    public void initialise() {
        playerConfig = config.getPlayer();

        username = find("username", UIText.class);
        username.setText(playerConfig.getName());
        username.bindTooltipString(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return validateUserName();
            }
        });

        UIButton okButton = find("ok", UIButton.class);
        if (okButton != null) {
            okButton.subscribe(button -> {
                playerConfig.setName(username.getText().trim());
                playerConfig.setHasEnteredUsername(true);
                getManager().popScreen();
            });
            okButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return Strings.isNullOrEmpty(validateUserName());
                }
            });
            okButton.setTooltipDelay(0);
            okButton.bindTooltipString(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return validateUserName();
                }
            });
        }

        WidgetUtil.trySubscribe(this, "cancel", button -> {
            playerConfig.setHasEnteredUsername(true);
            getManager().popScreen();
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (username != null) {
            username.setText(config.getPlayer().getName());
        }
    }

  private String validateUserName() {
        if (username != null) {
            if (Strings.isNullOrEmpty(username.getText()) || username.getText().trim().length() == 0) {
                return translationSystem.translate("${engine:menu#missing-name-message}");
            }
            if (username.getText().trim().length() > 100) {
                return translationSystem.translate("${engine:menu#validation-username-max-length}");
            }
        }
        return null;
    }
}

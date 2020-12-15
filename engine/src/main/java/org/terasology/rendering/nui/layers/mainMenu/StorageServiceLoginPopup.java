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
import org.terasology.config.Config;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIText;

import java.net.MalformedURLException;
import java.net.URL;

public class StorageServiceLoginPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:StorageServiceLoginPopup!instance");

    @In
    private Config config;

    @In
    private StorageServiceWorker storageService;

    @In
    private TranslationSystem translationSystem;

    @Override
    public void initialise() {
        UIText url = find("url", UIText.class);
        UIText login = find("login", UIText.class);
        UIText password = find("password", UIText.class);

        find("existing-identities-warning", UILabel.class).setVisible(!config.getSecurity().getAllIdentities().isEmpty());

        WidgetUtil.trySubscribe(this, "cancel", widget -> getManager().popScreen());
        WidgetUtil.trySubscribe(this, "ok", widget -> {
            try {
                storageService.login(new URL(url.getText()), login.getText(), password.getText());
                getManager().popScreen();
            } catch (MalformedURLException e) {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage(
                        translationSystem.translate("${engine:menu#error}"),
                        translationSystem.translate("${engine:menu#storage-service-popup-bad-url}")
                );
            }
        });
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIText;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

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

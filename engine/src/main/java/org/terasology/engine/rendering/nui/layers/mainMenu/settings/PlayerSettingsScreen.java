// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.settings;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorkerStatus;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.StorageServiceLoginPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.ThreeButtonPopup;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;

import static org.terasology.engine.identity.storageServiceClient.StatusMessageTranslator.getLocalizedButtonMessage;
import static org.terasology.engine.identity.storageServiceClient.StatusMessageTranslator.getLocalizedStatusMessage;

public class PlayerSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:PlayerMenuScreen");

    @In
    private Context context;
    @In
    private PlayerConfig config;
    @In
    private SystemConfig systemConfig;
    @In
    private TranslationSystem translationSystem;
    @In
    private StorageServiceWorker storageService;

    private UILabel storageServiceStatus;
    private UIButton storageServiceAction;

    private StorageServiceWorkerStatus storageServiceWorkerStatus;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        storageServiceStatus = find("storageServiceStatus", UILabel.class);
        storageServiceAction = find("storageServiceAction", UIButton.class);
        updateStorageServiceStatus();

        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());

        IdentityIOHelper identityIOHelper = new IdentityIOHelper(context);
        WidgetUtil.trySubscribe(this, "importIdentities", button -> identityIOHelper.importIdentities());
        WidgetUtil.trySubscribe(this, "exportIdentities", button -> identityIOHelper.exportIdentities());

        WidgetUtil.trySubscribe(this, "storageServiceAction", widget -> {
            if (storageService.getStatus() == StorageServiceWorkerStatus.LOGGED_IN) {
                ThreeButtonPopup logoutPopup = getManager().pushScreen(ThreeButtonPopup.ASSET_URI, ThreeButtonPopup.class);
                logoutPopup.setMessage(translationSystem.translate("${engine:menu#storage-service-log-out}"),
                        translationSystem.translate("${engine:menu#storage-service-log-out-popup}"));
                logoutPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), () -> storageService.logout(true));
                logoutPopup.setCenterButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> storageService.logout(false));
                logoutPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-cancel}"), () -> { });
            } else if (storageService.getStatus() == StorageServiceWorkerStatus.LOGGED_OUT) {
                getManager().pushScreen(StorageServiceLoginPopup.ASSET_URI, StorageServiceLoginPopup.class);
            }
        });

    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (storageService.getStatus() != storageServiceWorkerStatus) {
            updateStorageServiceStatus();
        }
    }

    private void updateStorageServiceStatus() {
        StorageServiceWorkerStatus stat = storageService.getStatus();
        storageServiceStatus.setText(getLocalizedStatusMessage(stat, translationSystem, storageService.getLoginName()));
        storageServiceAction.setText(getLocalizedButtonMessage(stat, translationSystem));
        storageServiceAction.setVisible(stat.isButtonEnabled());
        storageServiceWorkerStatus = stat;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

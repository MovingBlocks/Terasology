// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.NonNativeJVMDetector;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorkerStatus;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.PlayerSettingsScreen;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.SettingsMenuScreen;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.version.TerasologyVersion;

import static org.terasology.engine.identity.storageServiceClient.StatusMessageTranslator.getLocalizedStatusMessage;

public class MainMenuScreen extends CoreScreenLayer {

    @In
    private GameEngine engine;

    @In
    private StorageServiceWorker storageService;

    @In
    private TranslationSystem translationSystem;

    private UILabel storageServiceStatus;
    private StorageServiceWorkerStatus storageServiceWorkerStatus; //keep track of previous status to avoid performance drop due to updating UI when no change happened

    @Override
    public void initialise() {

        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        storageServiceStatus = find("storageServiceStatus", UILabel.class);
        updateStorageServiceStatus();

        UILabel versionLabel = find("version", UILabel.class);
        versionLabel.setText(TerasologyVersion.getInstance().getHumanVersion());

        UILabel jvmWarningLabel = find("nonNativeJvmWarning", UILabel.class);
        jvmWarningLabel.setVisible(NonNativeJVMDetector.JVM_ARCH_IS_NONNATIVE);

        SelectGameScreen selectScreen = getManager().createScreen(SelectGameScreen.ASSET_URI, SelectGameScreen.class);
      
        UniverseWrapper universeWrapper = new UniverseWrapper();
      
        WidgetUtil.trySubscribe(this, "singleplayer", button -> {
            universeWrapper.setLoadingAsServer(false);
            selectScreen.setUniverseWrapper(universeWrapper);
            triggerForwardAnimation(selectScreen);
        });
        WidgetUtil.trySubscribe(this, "multiplayer", button -> {
            universeWrapper.setLoadingAsServer(true);
            selectScreen.setUniverseWrapper(universeWrapper);
            triggerForwardAnimation(selectScreen);
        });
        WidgetUtil.trySubscribe(this, "join", button -> {
            if (storageService.getStatus() == StorageServiceWorkerStatus.WORKING) {
                ConfirmPopup confirmPopup = getManager().pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
                confirmPopup.setMessage(translationSystem.translate("${engine:menu#warning}"), translationSystem.translate("${engine:menu#storage-service-working}"));
                confirmPopup.setOkHandler(() -> triggerForwardAnimation(JoinGameScreen.ASSET_URI));
            } else {
                triggerForwardAnimation(JoinGameScreen.ASSET_URI);
            }
        });
        WidgetUtil.trySubscribe(this, "settings", button -> triggerForwardAnimation(SettingsMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "extras", button->triggerForwardAnimation(ExtrasMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "exit", button -> engine.shutdown());
        WidgetUtil.trySubscribe(this, "storageServiceAction", widget -> triggerForwardAnimation(PlayerSettingsScreen.ASSET_URI));
    }

    private void updateStorageServiceStatus() {
        StorageServiceWorkerStatus stat = storageService.getStatus();
        storageServiceStatus.setText(translationSystem.translate("${engine:menu#storage-service}") + ": " +
                getLocalizedStatusMessage(stat, translationSystem, storageService.getLoginName()));
        storageServiceWorkerStatus = stat;
    }

    @Override
    public void onOpened() {
        super.onOpened();
        getAnimationSystem().skip();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (storageService.getStatus() != storageServiceWorkerStatus) {
            updateStorageServiceStatus();
        }
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}



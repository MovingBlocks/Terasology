// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.engine.GameEngine;
import org.terasology.engine.NonNativeJVMDetector;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.identity.storageServiceClient.StorageServiceWorkerStatus;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UILabel;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.settings.PlayerSettingsScreen;
import org.terasology.rendering.nui.layers.mainMenu.settings.SettingsMenuScreen;
import org.terasology.version.TerasologyVersion;

public class MainMenuScreen extends CoreScreenLayer {

    @In
    private GameEngine engine;

    @In
    private StorageServiceWorker storageService;

    @In
    private TranslationSystem translationSystem;

    @Override
    public void initialise() {

        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

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
                confirmPopup.setMessage(translationSystem.translate("${engine:menu#warning}"),
                        translationSystem.translate("${engine:menu#storage-service-working}"));
                confirmPopup.setOkHandler(() -> triggerForwardAnimation(JoinGameScreen.ASSET_URI));
            } else {
                triggerForwardAnimation(JoinGameScreen.ASSET_URI);
            }
        });
        WidgetUtil.trySubscribe(this, "settings", button -> triggerForwardAnimation(SettingsMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "extras", button -> triggerForwardAnimation(ExtrasMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "exit", button -> engine.shutdown());
        WidgetUtil.trySubscribe(this, "storageServiceAction",
                widget -> triggerForwardAnimation(PlayerSettingsScreen.ASSET_URI));
    }

    @Override
    public void onOpened() {
        super.onOpened();
        getAnimationSystem().skip();
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



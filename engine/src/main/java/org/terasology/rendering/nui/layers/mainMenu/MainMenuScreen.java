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

package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.NonNativeJVMDetector;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.settings.SettingsMenuScreen;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.version.TerasologyVersion;

/**
 */
public class MainMenuScreen extends CoreScreenLayer {

    @In
    private GameEngine engine;

    @In
    private StorageServiceWorker storageService;

    @In
    private TranslationSystem translationSystem;

    private UILabel storageServiceStatus;
    private UIButton storageServiceAction;
    private boolean storageServicePreviousLoggedIn; //keep track of previous status to avoid performance drop due to updating UI when no change happened

    @Override
    public void initialise() {

        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        storageServiceStatus = find("storageServiceStatus", UILabel.class);
        storageServiceAction = find("storageServiceAction", UIButton.class);
        updateStorageServiceStatus();

        UILabel versionLabel = find("version", UILabel.class);
        versionLabel.setText(TerasologyVersion.getInstance().getHumanVersion());

        UILabel jvmWarningLabel = find("nonNativeJvmWarning", UILabel.class);
        jvmWarningLabel.setVisible(NonNativeJVMDetector.JVM_ARCH_IS_NONNATIVE);

        SelectGameScreen selectScreen = getManager().createScreen(SelectGameScreen.ASSET_URI, SelectGameScreen.class);

        WidgetUtil.trySubscribe(this, "singleplayer", button -> {
            selectScreen.setLoadingAsServer(false);
            triggerForwardAnimation(selectScreen);
        });
        WidgetUtil.trySubscribe(this, "multiplayer", button -> {
            selectScreen.setLoadingAsServer(true);
            triggerForwardAnimation(selectScreen);
        });
        WidgetUtil.trySubscribe(this, "join", button -> triggerForwardAnimation(JoinGameScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "settings", button -> triggerForwardAnimation(SettingsMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "credits", button -> triggerForwardAnimation(CreditsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "exit", button -> engine.shutdown());
        WidgetUtil.trySubscribe(this, "crashReporter", widget -> CrashReporter.report(new Throwable("Report an error."), LoggingContext.getLoggingPath()));
        WidgetUtil.trySubscribe(this, "storageServiceAction", widget -> {
            if (storageService.isLoggedIn()) {
                storageService.logout();
            } else {
                getManager().pushScreen(StorageServiceLoginPopup.ASSET_URI, StorageServiceLoginPopup.class);
            }
        });
    }

    private void updateStorageServiceStatus() {
        if (storageService.isLoggedIn()) {
            storageServiceStatus.setText(translationSystem.translate("${engine:menu#storage-service-logged-in}") + storageService.getLoginName());
            storageServiceAction.setText(translationSystem.translate("${engine:menu#storage-service-log-out}"));
        } else {
            storageServiceStatus.setText(translationSystem.translate("${engine:menu#storage-service-logged-out}"));
            storageServiceAction.setText(translationSystem.translate("${engine:menu#storage-service-log-in}"));
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();
        getAnimationSystem().skip();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (storageService.isLoggedIn() != storageServicePreviousLoggedIn) {
            updateStorageServiceStatus();
            storageServicePreviousLoggedIn = !storageServicePreviousLoggedIn;
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



// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes;

import com.google.common.base.MoreObjects;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.TelemetryConfig;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.modes.loadProcesses.RegisterInputSystem;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.engine.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.engine.rendering.nui.layers.mainMenu.LaunchPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.telemetry.TelemetryScreen;
import org.terasology.engine.telemetry.TelemetryUtils;
import org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.engine.utilities.Assets;

/**
 * The class implements the main game menu.
 */
public class StateMainMenu extends AbstractState {
    private NUIManager nuiManager;
    private InputSystem inputSystem;
    private Console console;
    private StorageServiceWorker storageServiceWorker;

    private String messageOnLoad = "";
    private boolean headless;


    public StateMainMenu() {
    }

    public StateMainMenu(String showMessageOnLoad) {
        messageOnLoad = showMessageOnLoad;
    }

    @Override
    public void init(GameEngine gameEngine) {
        context = gameEngine.createChildContext();
        headless = context.get(DisplayDevice.class).isHeadless();

        initEntityAndComponentManagers(headless);

        createLocalPlayer(context);

        if (!headless) {
            // TODO: REMOVE this and handle refreshing of core game state at the engine level - see Issue #1127
            new RegisterInputSystem(context).step();

            nuiManager = context.get(NUIManager.class);
            eventSystem.registerEventHandler(nuiManager);
            NUIEditorSystem nuiEditorSystem = new NUIEditorSystem();
            context.put(NUIEditorSystem.class, nuiEditorSystem);
            componentSystemManager.register(nuiEditorSystem, "engine:NUIEditorSystem");

            NUISkinEditorSystem nuiSkinEditorSystem = new NUISkinEditorSystem();
            context.put(NUISkinEditorSystem.class, nuiSkinEditorSystem);
            componentSystemManager.register(nuiSkinEditorSystem, "engine:NUISkinEditorSystem");

            inputSystem = context.get(InputSystem.class);
        }

        componentSystemManager.initialise();
        console = context.get(Console.class);
        storageServiceWorker = context.get(StorageServiceWorker.class);
        playBackgroundMusic();

        if (!headless) {
            //guiManager.openWindow("main");
            context.get(NUIManager.class).pushScreen("engine:mainMenuScreen");
        }
        if (!messageOnLoad.isEmpty()) {
            TranslationSystem translationSystem = context.get(TranslationSystem.class);
            if (headless) {
                throw new RuntimeException("Game receive error and fall to main menu. See logs before");
            } else {
                MessagePopup popup = nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                popup.setMessage("Error", translationSystem.translate(messageOnLoad));
            }
        }

        // TODO: enable it when exposing the telemetry to users
        // pushLaunchPopup();
    }

    private void pushLaunchPopup() {
        Config config = context.get(Config.class);
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        TranslationSystem translationSystem = context.get(TranslationSystem.class);
        TelemetryLogstashAppender appender = TelemetryUtils.fetchTelemetryLogstashAppender();
        if (!telemetryConfig.isLaunchPopupDisabled()) {
            String telemetryTitle = translationSystem.translate("${engine:menu#telemetry-launch-popup-title}");
            String telemetryMessage = translationSystem.translate("${engine:menu#telemetry-launch-popup-text}");
            LaunchPopup telemetryConfirmPopup = nuiManager.pushScreen(LaunchPopup.ASSET_URI, LaunchPopup.class);
            telemetryConfirmPopup.setMessage(telemetryTitle, telemetryMessage);
            telemetryConfirmPopup.setYesHandler(() -> {
                telemetryConfig.setTelemetryAndErrorReportingEnable(true);

                // Enable error reporting
                appender.start();
            });
            telemetryConfirmPopup.setNoHandler(() -> {
                telemetryConfig.setTelemetryAndErrorReportingEnable(false);

                // Disable error reporting
                appender.stop();
            });
            telemetryConfirmPopup.setOptionButtonText(translationSystem.translate("${engine:menu#telemetry-button}"));
            telemetryConfirmPopup.setOptionHandler(() -> {
                nuiManager.pushScreen(TelemetryScreen.ASSET_URI, TelemetryScreen.class);
            });
        }
    }

    @Override
    public void dispose(boolean shuttingDown) {
        stopBackgroundMusic();
        if (nuiManager != null) {
            nuiManager.clear();
        }
        super.dispose(shuttingDown);
    }

    private void playBackgroundMusic() {
        context.get(AudioManager.class).loopMusic(Assets.getMusic("engine:MenuTheme").get());
    }

    private void stopBackgroundMusic() {
        context.get(AudioManager.class).stopAllSounds();
    }

    @Override
    public void handleInput(float delta) {
        if (inputSystem != null) {
            inputSystem.update(delta);
        }
    }

    @Override
    public void update(float delta) {
        updateUserInterface(delta);

        eventSystem.process();
        storageServiceWorker.flushNotificationsToConsole(console);
    }

    @Override
    public void render() {
        if (nuiManager != null) {
            nuiManager.render();
        }
    }

    @Override
    public String getLoggingPhase() {
        return LoggingContext.MENU;
    }

    @Override
    public boolean isHibernationAllowed() {
        return !headless;
    }

    private void updateUserInterface(float delta) {
        if (nuiManager != null) {
            nuiManager.update(delta);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("messageOnLoad", messageOnLoad)
                .toString();
    }
}

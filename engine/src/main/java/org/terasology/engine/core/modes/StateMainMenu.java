// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes;

import com.google.common.base.MoreObjects;
import org.terasology.context.Lifetime;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.players.LocalPlayerSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.engine.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.di.ServiceRegistry;

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
        ServiceRegistry serviceRegistry = new ServiceRegistry();

        headless = context.get(DisplayDevice.class).isHeadless();

        registerEntityAndComponentManagers(headless, serviceRegistry);
        registerLocalPlayer(serviceRegistry);

        if (!headless) {
            NUIEditorSystem nuiEditorSystem = new NUIEditorSystem();
            serviceRegistry.with(NUIEditorSystem.class).lifetime(Lifetime.Singleton).use(() -> nuiEditorSystem);
            NUISkinEditorSystem nuiSkinEditorSystem = new NUISkinEditorSystem();
            serviceRegistry.with(NUISkinEditorSystem.class).lifetime(Lifetime.Singleton).use(() -> nuiSkinEditorSystem);

            LocalPlayerSystem localPlayerSystem = new LocalPlayerSystem();
            serviceRegistry.with(LocalPlayerSystem.class).lifetime(Lifetime.Singleton).use(() -> localPlayerSystem);

            CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
            serviceRegistry.with(CameraTargetSystem.class).lifetime(Lifetime.Singleton).use(() -> cameraTargetSystem);
        }

        context = gameEngine.createImmutableChildContext(serviceRegistry);
        CoreRegistry.setContext(context);

        initEntityAndComponentManagers();
        createLocalPlayer();

        if (!headless) {
            componentSystemManager.register(context.get(LocalPlayerSystem.class), "engine:localPlayerSystem");
            componentSystemManager.register(context.get(CameraTargetSystem.class), "engine:CameraTargetSystem");

            inputSystem = context.get(InputSystem.class);
            componentSystemManager.register(inputSystem, "engine:InputSystem");

            nuiManager = context.get(NUIManager.class);
            eventSystem.registerEventHandler(nuiManager);
            componentSystemManager.register(context.get(NUIEditorSystem.class), "engine:NUIEditorSystem");
            componentSystemManager.register(context.get(NUISkinEditorSystem.class), "engine:NUISkinEditorSystem");
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
                throw new RuntimeException(
                        String.format(
                                "Game could not be started, server attempted to return to main menu: [%s]. See logs before",
                                translationSystem.translate(messageOnLoad)
                        ));
            } else {
                MessagePopup popup = nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                popup.setMessage("Error", translationSystem.translate(messageOnLoad));
            }
        }

        // TODO: enable it when exposing the telemetry to users
        // pushLaunchPopup();
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

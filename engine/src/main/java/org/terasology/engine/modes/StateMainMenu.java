// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes;

import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.TelemetryConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.loadProcesses.RegisterInputSystem;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.engine.logic.console.ConsoleSystem;
import org.terasology.engine.logic.console.commands.CoreCommands;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.engine.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.engine.rendering.nui.layers.mainMenu.LaunchPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.telemetry.TelemetryScreen;
import org.terasology.engine.telemetry.TelemetryUtils;
import org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.engine.utilities.Assets;

/**
 * The class implements the main game menu.
 * <br><br>
 *
 * @version 0.3
 */
public class StateMainMenu implements GameState {
    private Context context;
    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;
    private NUIManager nuiManager;
    private InputSystem inputSystem;
    private Console console;
    private StorageServiceWorker storageServiceWorker;

    private String messageOnLoad = "";


    public StateMainMenu() {
    }

    public StateMainMenu(String showMessageOnLoad) {
        messageOnLoad = showMessageOnLoad;
    }

    @Override
    public void init(GameEngine gameEngine) {
        context = gameEngine.createChildContext();
        CoreRegistry.setContext(context);

        //let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);

        eventSystem = context.get(EventSystem.class);
        console = new ConsoleImpl(context);
        context.put(Console.class, console);

        nuiManager = new NUIManagerInternal((TerasologyCanvasRenderer) context.get(CanvasRenderer.class), context);
        context.put(NUIManager.class, nuiManager);

        eventSystem.registerEventHandler(nuiManager);

        componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);

        // TODO: Reduce coupling between Input system and CameraTargetSystem,
        // TODO: potentially eliminating the following lines. See Issue #1126
        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        context.put(CameraTargetSystem.class, cameraTargetSystem);

        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");
        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");

        NUIEditorSystem nuiEditorSystem = new NUIEditorSystem();
        context.put(NUIEditorSystem.class, nuiEditorSystem);
        componentSystemManager.register(nuiEditorSystem, "engine:NUIEditorSystem");

        NUISkinEditorSystem nuiSkinEditorSystem = new NUISkinEditorSystem();
        context.put(NUISkinEditorSystem.class, nuiSkinEditorSystem);
        componentSystemManager.register(nuiSkinEditorSystem, "engine:NUISkinEditorSystem");

        inputSystem = context.get(InputSystem.class);

        // TODO: REMOVE this and handle refreshing of core game state at the engine level - see Issue #1127
        new RegisterInputSystem(context).step();

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class), context.get(RecordAndReplayCurrentStatus.class));
        context.put(LocalPlayer.class, localPlayer);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        storageServiceWorker = context.get(StorageServiceWorker.class);

        playBackgroundMusic();

        //guiManager.openWindow("main");
        context.get(NUIManager.class).pushScreen("engine:mainMenuScreen");
        if (!messageOnLoad.isEmpty()) {
            TranslationSystem translationSystem = context.get(TranslationSystem.class);
            MessagePopup popup = nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage("Error", translationSystem.translate(messageOnLoad));
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
            telemetryConfirmPopup.setOptionHandler(()-> {
                nuiManager.pushScreen(TelemetryScreen.ASSET_URI, TelemetryScreen.class);
            });
        }
    }

    @Override
    public void dispose(boolean shuttingDown) {
        // Apparently this can be disposed of before it is completely initialized? Probably only during
        // crashes, but crashing again during shutdown complicates the diagnosis.
        if (eventSystem != null) {
            eventSystem.process();
        }
        if (componentSystemManager != null) {
            componentSystemManager.shutdown();
        }
        stopBackgroundMusic();

        if (nuiManager != null) {
            nuiManager.clear();
        }
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    private void playBackgroundMusic() {
        context.get(AudioManager.class).loopMusic(Assets.getMusic("engine:MenuTheme").get());
    }

    private void stopBackgroundMusic() {
        context.get(AudioManager.class).stopAllSounds();
    }

    @Override
    public void handleInput(float delta) {
        inputSystem.update(delta);
    }

    @Override
    public void update(float delta) {
        updateUserInterface(delta);

        eventSystem.process();
        storageServiceWorker.flushNotificationsToConsole(console);

    }

    @Override
    public void render() {
        nuiManager.render();
    }

    @Override
    public String getLoggingPhase() {
        return LoggingContext.MENU;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

    private void updateUserInterface(float delta) {
        nuiManager.update(delta);
    }
}

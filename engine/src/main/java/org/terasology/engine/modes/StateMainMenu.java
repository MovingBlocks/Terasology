// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes;

import org.terasology.audio.AudioManager;
import org.terasology.config.Config;
import org.terasology.config.TelemetryConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.loadProcesses.RegisterInputSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.logic.console.ConsoleSystem;
import org.terasology.logic.console.commands.CoreCommands;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layers.mainMenu.LaunchPopup;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.telemetry.TelemetryScreen;
import org.terasology.telemetry.TelemetryUtils;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.utilities.Assets;

/**
 * The class implements the main game menu.
 * <br><br>
 *
 * @version 0.3
 */
public class StateMainMenu implements GameState {

    @In
    DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    @In
    RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;
    @In
    private InputSystem inputSystem;
    @In
    private ContextAwareClassFactory classFactory;
    @In
    private StorageServiceWorker storageServiceWorker;
    @In
    private AudioManager audioManager;
    @In
    private Config config;
    @In
    private TranslationSystem translationSystem;
    private EventSystem eventSystem;
    private EngineEntityManager entityManager;
    private Context context;
    private ComponentSystemManager componentSystemManager;
    private NUIManager nuiManager;
    private Console console;

    private String messageOnLoad = "";


    public StateMainMenu() {
    }

    public StateMainMenu(String showMessageOnLoad) {
        messageOnLoad = showMessageOnLoad;
    }

    @Override
    public void init(GameEngine gameEngine) {
        context = gameEngine.createChildContext();
        updateContext(context);

        //let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context); // provides EngineEntityManager and
        // EventSystem
        entityManager = context.get(EngineEntityManager.class);
        eventSystem = context.get(EventSystem.class);

        console = classFactory.createToContext(ConsoleImpl.class, Console.class);

        nuiManager = classFactory.createToContext(NUIManagerInternal.class, NUIManager.class); // TODO
        // handle context in NuiManager.
        eventSystem.registerEventHandler(nuiManager);

        componentSystemManager = classFactory.createToContext(ComponentSystemManager.class); // TODO handle
        // context in ComponentSystemManager

        // TODO: Reduce coupling between Input system and CameraTargetSystem,
        // TODO: potentially eliminating the following lines. See Issue #1126

        componentSystemManager.register(classFactory.createToContext(CameraTargetSystem.class),
                "engine:CameraTargetSystem");
        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");
        componentSystemManager.register(classFactory.createToContext(NUIEditorSystem.class),
                "engine:NUIEditorSystem");
        componentSystemManager.register(classFactory.createToContext(NUISkinEditorSystem.class),
                "engine:NUISkinEditorSystem");

        // TODO: REMOVE this and handle refreshing of core game state at the engine level - see Issue #1127
        classFactory.createWithContext(RegisterInputSystem.class).step();

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());

        LocalPlayer localPlayer = classFactory.createToContext(LocalPlayer.class);
        localPlayer.setRecordAndReplayClasses(directionAndOriginPosRecorderList, recordAndReplayCurrentStatus);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        playBackgroundMusic();

        nuiManager.pushScreen("engine:mainMenuScreen");
        if (!messageOnLoad.isEmpty()) {
            MessagePopup popup = nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage("Error", translationSystem.translate(messageOnLoad));
        }

        // TODO: enable it when exposing the telemetry to users
        // pushLaunchPopup();
    }

    private void pushLaunchPopup() {
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
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
            telemetryConfirmPopup.setOptionHandler(() ->
                    nuiManager.pushScreen(TelemetryScreen.ASSET_URI, TelemetryScreen.class)
            );
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
        audioManager.loopMusic(Assets.getMusic("engine:MenuTheme").get());
    }

    private void stopBackgroundMusic() {
        audioManager.stopAllSounds();
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

    private void updateContext(Context context) {
        /*
         * We can't load the engine without core registry yet.
         * e.g. the statically created MaterialLoader needs the CoreRegistry to get the AssetManager.
         * And the engine loads assets while it gets created.
         */
        // TODO: Remove
        CoreRegistry.setContext(context);
        classFactory.setCurrentContext(context);
    }
}

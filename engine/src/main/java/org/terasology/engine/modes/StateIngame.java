/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.engine.modes;

import org.terasology.audio.AudioManager;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.GameThread;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.game.GameManifest;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.Console;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.StorageManager;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;
import org.terasology.world.chunks.ChunkProvider;

import java.util.Collections;

/**
 * Play mode.
 *
 * @version 0.1
 */
public class StateIngame implements GameState {

    private ComponentSystemManager componentSystemManager;
    private EventSystem eventSystem;
    private NUIManager nuiManager;
    private WorldRenderer worldRenderer;
    private EngineEntityManager entityManager;
    private CameraTargetSystem cameraTargetSystem;
    private InputSystem inputSystem;
    private NetworkSystem networkSystem;
    private StorageServiceWorker storageServiceWorker;
    private Console console;
    private Context context;

    /* GAME LOOP */
    private boolean pauseGame;

    private StorageManager storageManager;

    private GameManifest gameManifest;

    public StateIngame(GameManifest gameManifest, Context context) {
        this.gameManifest = gameManifest;
        this.context = context;
    }

    @Override
    public void init(GameEngine engine) {
        // context from loading state gets used.
        nuiManager = context.get(NUIManager.class);
        worldRenderer = context.get(WorldRenderer.class);
        eventSystem = context.get(EventSystem.class);
        componentSystemManager = context.get(ComponentSystemManager.class);
        entityManager = context.get(EngineEntityManager.class);
        cameraTargetSystem = context.get(CameraTargetSystem.class);
        inputSystem = context.get(InputSystem.class);
        eventSystem.registerEventHandler(nuiManager);
        networkSystem = context.get(NetworkSystem.class);
        storageManager = context.get(StorageManager.class);
        storageServiceWorker = context.get(StorageServiceWorker.class);
        console = context.get(Console.class);

        // Show or hide the HUD according to the settings
        nuiManager.getHUD().bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return !context.get(Config.class).getRendering().getDebug().isHudHidden();
            }
        });

        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            String motd = networkSystem.getServer().getInfo().getMOTD();
            if (motd != null && motd.length() != 0) {
                nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Server MOTD", motd);
            }
        }
    }

    @Override
    public void dispose(boolean shuttingDown) {
        ChunkProvider chunkProvider = context.get(ChunkProvider.class);
        chunkProvider.dispose();

        boolean save = networkSystem.getMode().isAuthority();
        if (save) {
            storageManager.waitForCompletionOfPreviousSaveAndStartSaving();
        }

        networkSystem.shutdown();
        // TODO: Shutdown background threads
        eventSystem.process();
        GameThread.processWaitingProcesses();
        nuiManager.clear();

        context.get(AudioManager.class).stopAllSounds();

        if (worldRenderer != null) {
            worldRenderer.dispose();
            worldRenderer = null;
        }
        componentSystemManager.shutdown();

        context.get(PhysicsEngine.class).dispose();

        entityManager.clear();

        if (storageManager != null) {
            storageManager.finishSavingAndShutdown();
        }

        ModuleEnvironment oldEnvironment = context.get(ModuleManager.class).getEnvironment();
        context.get(ModuleManager.class).loadEnvironment(Collections.<Module>emptySet(), true);
        if (!shuttingDown) {
            context.get(EnvironmentSwitchHandler.class).handleSwitchToEmptyEnvironment(context);
        }
        if (oldEnvironment != null) {
            oldEnvironment.close();
        }
        console.dispose();
        GameThread.clearWaitingProcesses();

        /*
         * Clear the binding as otherwise the complete ingame state would be
         * referenced.
         */
        nuiManager.getHUD().clearVisibleBinding();
    }

    @Override
    public void update(float delta) {

        eventSystem.process();

        for (UpdateSubscriberSystem system : componentSystemManager.iterateUpdateSubscribers()) {
            PerformanceMonitor.startActivity(system.getClass().getSimpleName());
            system.update(delta);
            PerformanceMonitor.endActivity();
        }

        if (worldRenderer != null && shouldUpdateWorld()) {
            worldRenderer.update(delta);
        }
        if (storageManager != null) {
            storageManager.update();
        }


        updateUserInterface(delta);

        storageServiceWorker.flushNotificationsToConsole(console);
    }


    @Override
    public void handleInput(float delta) {
        cameraTargetSystem.update(delta);
        inputSystem.update(delta);
    }

    private boolean shouldUpdateWorld() {
        return !pauseGame;
    }

    @Override
    public void render() {
        DisplayDevice display = context.get(DisplayDevice.class);
        display.prepareToRender();

        if (worldRenderer != null) {
            if (!context.get(Config.class).getRendering().isVrSupport()) {
                worldRenderer.render(RenderingStage.MONO);
            } else {
                worldRenderer.render(RenderingStage.LEFT_EYE);
                worldRenderer.render(RenderingStage.RIGHT_EYE);
            }
        }

        /* UI */
        PerformanceMonitor.startActivity("Render and Update UI");
        renderUserInterface();
        PerformanceMonitor.endActivity();
    }

    @Override
    public boolean isHibernationAllowed() {
        return networkSystem.getMode() == NetworkMode.NONE;
    }

    @Override
    public String getLoggingPhase() {
        return gameManifest.getTitle();
    }

    private void renderUserInterface() {
        PerformanceMonitor.startActivity("Rendering NUI");
        nuiManager.render();
        PerformanceMonitor.endActivity();
    }

    private void updateUserInterface(float delta) {
        nuiManager.update(delta);
    }

    public void pause() {
        pauseGame = true;
    }

    private void unpause() {
        pauseGame = false;
    }

    public void togglePauseGame() {
        if (pauseGame) {
            unpause();
        } else {
            pause();
        }
    }

    public boolean isGamePaused() {
        return pauseGame;
    }

    public Context getContext() {
        return context;
    }
}

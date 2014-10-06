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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.TeraOVR;
import org.terasology.asset.AssetManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.GameThread;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.game.Game;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.Console;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.persistence.PlayerStore;
import org.terasology.persistence.StorageManager;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkProvider;

import java.io.IOException;
import java.util.Collections;

/**
 * Play mode.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public class StateIngame implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateIngame.class);

    private ComponentSystemManager componentSystemManager;
    private EventSystem eventSystem;
    private NUIManager nuiManager;
    private WorldRenderer worldRenderer;
    private EngineEntityManager entityManager;
    private CameraTargetSystem cameraTargetSystem;
    private InputSystem inputSystem;
    private NetworkSystem networkSystem;
    private Config config;

    /* GAME LOOP */
    private boolean pauseGame;
    /**
     * Time of the next save in the format that {@link System#currentTimeMillis()} returns.
     */
    private Long nextAutoSave;
    private StorageManager storageManager;

    public StateIngame() {
    }

    public void init(GameEngine engine) {
        nuiManager = CoreRegistry.get(NUIManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        eventSystem = CoreRegistry.get(EventSystem.class);
        componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        entityManager = (EngineEntityManager) CoreRegistry.get(EntityManager.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        inputSystem = CoreRegistry.get(InputSystem.class);
        eventSystem.registerEventHandler(nuiManager);
        networkSystem = CoreRegistry.get(NetworkSystem.class);
        config = CoreRegistry.get(Config.class);
        storageManager = CoreRegistry.get(StorageManager.class);

        if (CoreRegistry.get(Config.class).getRendering().isOculusVrSupport()
                && OculusVrHelper.isNativeLibraryLoaded()) {

            logger.info("Trying to initialize Oculus SDK...");
            TeraOVR.initSDK();

            logger.info("Updating Oculus projection parameters from device...");
            OculusVrHelper.updateFromDevice();
        }
        // Show or hide the HUD according to the settings
        nuiManager.getHUD().bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return !CoreRegistry.get(Config.class).getRendering().getDebug().isHudHidden();
            }
        });
    }

    @Override
    public void dispose() {
        if (CoreRegistry.get(Config.class).getRendering().isOculusVrSupport() && OculusVrHelper.isNativeLibraryLoaded()) {
            logger.info("Shutting down Oculus SDK...");
            TeraOVR.clear();
        }

        boolean save = networkSystem.getMode().isAuthority();
        networkSystem.shutdown();
        // TODO: Shutdown background threads
        eventSystem.process();
        GameThread.processWaitingProcesses();
        nuiManager.clear();

        if (worldRenderer != null) {
            worldRenderer.dispose();
            worldRenderer = null;
        }
        componentSystemManager.shutdown();

        if (save) {
            CoreRegistry.get(Game.class).save();
        }

        CoreRegistry.get(PhysicsEngine.class).dispose();

        entityManager.clear();
        ModuleEnvironment environment = CoreRegistry.get(ModuleManager.class).loadEnvironment(Collections.<Module>emptySet(), true);
        CoreRegistry.get(AssetManager.class).setEnvironment(environment);
        CoreRegistry.get(Console.class).dispose();
        CoreRegistry.clear();
        BlockManager.getAir().setEntity(EntityRef.NULL);
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

        if (isSavingNecessaryAndPossible()) {
            logger.info("Auto saving...");
            PerformanceMonitor.startActivity("Auto Saving");
            CoreRegistry.get(Game.class).save(false);
            /*
             * Saving without shutting down the threads of the storage manger is important, as otherwise the chunks don't get saved
             * TODO maybe reorder? and makit just not shutdown
             */
            CoreRegistry.get(ChunkProvider.class).saveChunks();
            for (Client client : networkSystem.getPlayers()) {
                PlayerStore playerStore = storageManager.createPlayerStoreForSave(client.getId());
                EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
                if (character.exists()) {
                    playerStore.setCharacter(character);
                }
                playerStore.save(false);
            }
            try {
                storageManager.flush();
                logger.info("Auto complete");
            } catch (IOException e) {
                logger.info("Auto save failed");
            }
            // TODO refactor: Game#save also flushes the storage manager
            scheduleNextAutoSave();
            PerformanceMonitor.endActivity();
        }

        updateUserInterface(delta);
    }

    @Override
    public void handleInput(float delta) {
        cameraTargetSystem.update(delta);
        inputSystem.update(delta);
    }

    private boolean shouldUpdateWorld() {
        return !pauseGame;
    }

    public void render() {
        DisplayDevice displayDevice = CoreRegistry.get(DisplayDevice.class);
        displayDevice.prepareToRender();

        if (worldRenderer != null) {
            if (!CoreRegistry.get(Config.class).getRendering().isOculusVrSupport()) {
                worldRenderer.render(DefaultRenderingProcess.StereoRenderState.MONO);
            } else {
                worldRenderer.render(DefaultRenderingProcess.StereoRenderState.OCULUS_LEFT_EYE);
                worldRenderer.render(DefaultRenderingProcess.StereoRenderState.OCULUS_RIGHT_EYE);
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

    public void renderUserInterface() {
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

    public void unpause() {
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


    private boolean isSavingNecessaryAndPossible() {
        if (!config.getSystem().isAutoSaveEnabled()) {
            return false;
        }
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        boolean isAuthority = networkSystem.getMode().isAuthority();
        if (!isAuthority ) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (nextAutoSave == null) {
            scheduleNextAutoSave();
            return false;
        }
        if (currentTime >= nextAutoSave) {
            return true;
        } else {
            return false;
        }
    }


    private void scheduleNextAutoSave() {
        long msBetweenAutoSave = config.getSystem().getSecondsBetweenAutoSave() * 1000;
        nextAutoSave = System.currentTimeMillis() + msBetweenAutoSave;
    }

}

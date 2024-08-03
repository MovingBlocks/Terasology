// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes;

import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.rendering.world.WorldRenderer.RenderingStage;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.nui.databinding.ReadOnlyBinding;

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
        if (nuiManager != null) {
            inputSystem = context.get(InputSystem.class);
            eventSystem.registerEventHandler(nuiManager);
        }
        networkSystem = context.get(NetworkSystem.class);
        storageManager = context.get(StorageManager.class);
        storageServiceWorker = context.get(StorageServiceWorker.class);
        console = context.get(Console.class);

        if (nuiManager != null) {
            // Show or hide the HUD according to the settings
            nuiManager.getHUD().bindVisible(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return !context.get(Config.class).getRendering().getDebug().isHudHidden();
                }
            });
        }

        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            String motd = networkSystem.getServer().getInfo().getMOTD();
            if (nuiManager != null && motd != null && motd.length() != 0) {
                nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Server MOTD", motd);
            }
        }
    }

    @Override
    public void dispose(boolean shuttingDown) {
        ChunkProvider chunkProvider = context.get(ChunkProvider.class);
        chunkProvider.dispose();

        AssetTypeManager assetTypeManager = context.get(ModuleAwareAssetTypeManager.class);
        // dispose all module assets
        assetTypeManager.getAssetTypes().forEach(assetType -> {
            for (ResourceUrn urn : assetType.getLoadedAssetUrns()) {
                if (!urn.getModuleName().equals(TerasologyConstants.ENGINE_MODULE)) {
                    assetType.getAsset(urn).ifPresent(Asset::dispose);
                }
            }
        });
        // dispose engine assets that should not be kept when switching game states
        assetTypeManager.getAssetType(BlockFamilyDefinition.class).ifPresent(AssetType::disposeAll);
        assetTypeManager.getAssetType(Prefab.class).ifPresent(AssetType::disposeAll);

        boolean save = networkSystem.getMode().isAuthority();
        if (save && storageManager != null) {
            storageManager.waitForCompletionOfPreviousSaveAndStartSaving();
        }

        networkSystem.shutdown();
        // TODO: Shutdown background threads
        eventSystem.process();
        GameThread.processWaitingProcesses();
        if (nuiManager != null) {
            nuiManager.clear();
        }

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

        if (nuiManager != null) {
            /*
             * Clear the binding as otherwise the complete ingame state would be
             * referenced.
             */
            nuiManager.getHUD().clearVisibleBinding();
        }
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

        if (nuiManager != null) {
            updateUserInterface(delta);
        }

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
            worldRenderer.render(RenderingStage.MONO);
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

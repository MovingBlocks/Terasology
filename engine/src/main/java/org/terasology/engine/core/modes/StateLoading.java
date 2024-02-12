// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.loadProcesses.AwaitCharacterSpawn;
import org.terasology.engine.core.modes.loadProcesses.CreateRemoteWorldEntity;
import org.terasology.engine.core.modes.loadProcesses.CreateWorldEntity;
import org.terasology.engine.core.modes.loadProcesses.EnsureSaveGameConsistency;
import org.terasology.engine.core.modes.loadProcesses.InitialiseBlockTypeEntities;
import org.terasology.engine.core.modes.loadProcesses.InitialiseCommandSystem;
import org.terasology.engine.core.modes.loadProcesses.InitialiseComponentSystemManager;
import org.terasology.engine.core.modes.loadProcesses.InitialiseEntitySystem;
import org.terasology.engine.core.modes.loadProcesses.InitialiseGraphics;
import org.terasology.engine.core.modes.loadProcesses.InitialisePhysics;
import org.terasology.engine.core.modes.loadProcesses.InitialiseRecordAndReplay;
import org.terasology.engine.core.modes.loadProcesses.InitialiseRemoteWorld;
import org.terasology.engine.core.modes.loadProcesses.InitialiseRendering;
import org.terasology.engine.core.modes.loadProcesses.InitialiseSystems;
import org.terasology.engine.core.modes.loadProcesses.InitialiseWorld;
import org.terasology.engine.core.modes.loadProcesses.InitialiseWorldGenerator;
import org.terasology.engine.core.modes.loadProcesses.JoinServer;
import org.terasology.engine.core.modes.loadProcesses.LoadEntities;
import org.terasology.engine.core.modes.loadProcesses.LoadExtraBlockData;
import org.terasology.engine.core.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.core.modes.loadProcesses.PostBeginSystems;
import org.terasology.engine.core.modes.loadProcesses.PreBeginSystems;
import org.terasology.engine.core.modes.loadProcesses.PrepareWorld;
import org.terasology.engine.core.modes.loadProcesses.ProcessBlockPrefabs;
import org.terasology.engine.core.modes.loadProcesses.RegisterBlockFamilies;
import org.terasology.engine.core.modes.loadProcesses.RegisterBlocks;
import org.terasology.engine.core.modes.loadProcesses.RegisterInputSystem;
import org.terasology.engine.core.modes.loadProcesses.RegisterMods;
import org.terasology.engine.core.modes.loadProcesses.RegisterSystems;
import org.terasology.engine.core.modes.loadProcesses.SetupLocalPlayer;
import org.terasology.engine.core.modes.loadProcesses.SetupRemotePlayer;
import org.terasology.engine.core.modes.loadProcesses.StartServer;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.game.Game;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.JoinStatus;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.engine.rendering.nui.layers.mainMenu.loadingScreen.LoadingScreen;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.nui.canvas.CanvasRenderer;

import java.util.Queue;

public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);
    private final GameManifest gameManifest;
    private final NetworkMode netMode;
    private final Queue<LoadProcess> loadProcesses = Queues.newArrayDeque();
    private Context context;
    private LoadProcess current;
    private JoinStatus joinStatus;

    private NUIManager nuiManager;

    private LoadingScreen loadingScreen;

    private SystemConfig systemConfig;

    private int progress;
    private int maxProgress;

    private boolean chunkGenerationStarted;
    private long timeLastChunkGenerated;
    private boolean headless;

    /**
     * Constructor for server or single player games
     */
    public StateLoading(GameManifest gameManifest, NetworkMode netMode) {
        Preconditions.checkArgument(netMode != NetworkMode.CLIENT);

        this.gameManifest = gameManifest;
        this.netMode = netMode;
    }

    /**
     * Constructor for client of multiplayer game
     */
    public StateLoading(JoinStatus joinStatus) {
        this.gameManifest = new GameManifest();
        this.netMode = NetworkMode.CLIENT;
        this.joinStatus = joinStatus;
    }

    @Override
    public void init(GameEngine engine) {
        this.context = engine.createChildContext();
        headless = context.get(DisplayDevice.class).isHeadless();
        
        CoreRegistry.setContext(context);
        context.getValue(NetworkSystem.class).setContext(context);
        systemConfig = context.get(SystemConfig.class);

        if (!headless) {
            this.nuiManager = new NUIManagerInternal((TerasologyCanvasRenderer) context.get(CanvasRenderer.class), context);
            context.put(NUIManager.class, nuiManager);
        }

        EngineTime time = (EngineTime) context.get(Time.class);
        time.setPaused(true);
        time.setGameTime(gameManifest.getTime());

        context.get(Game.class).load(gameManifest);
        switch (netMode) {
            case CLIENT:
                initClient();
                break;
            default:
                initHost();
                break;
        }

        progress = 0;
        maxProgress = 0;
        for (LoadProcess process : loadProcesses) {
            maxProgress += process.getExpectedCost();
        }

        popStep();
        if (nuiManager != null) {
            loadingScreen = nuiManager.pushScreen("engine:loadingScreen", LoadingScreen.class);
            loadingScreen.updateStatus(current.getMessage(), current.getProgress());
        }
        chunkGenerationStarted = false;
    }

    private void initClient() {
        loadProcesses.add(new JoinServer(context, gameManifest, joinStatus));
        if (!headless) {
            loadProcesses.add(new InitialiseRendering(context));
        }
        loadProcesses.add(new InitialiseEntitySystem(context));
        loadProcesses.add(new RegisterBlocks(context, gameManifest));
        if (!headless) {
            loadProcesses.add(new InitialiseGraphics(context));
        }
        loadProcesses.add(new LoadPrefabs(context));
        loadProcesses.add(new ProcessBlockPrefabs(context));
        loadProcesses.add(new LoadExtraBlockData(context));
        loadProcesses.add(new InitialiseComponentSystemManager(context));
        if (!headless) {
            loadProcesses.add(new RegisterInputSystem(context));
        }
        loadProcesses.add(new RegisterSystems(context, netMode));
        loadProcesses.add(new InitialiseCommandSystem(context));
        loadProcesses.add(new InitialiseRemoteWorld(context, gameManifest));
        loadProcesses.add(new InitialisePhysics(context));
        loadProcesses.add(new InitialiseSystems(context));
        loadProcesses.add(new PreBeginSystems(context));
        loadProcesses.add(new CreateRemoteWorldEntity(context));
        loadProcesses.add(new PostBeginSystems(context));
        loadProcesses.add(new SetupRemotePlayer(context));
        loadProcesses.add(new AwaitCharacterSpawn(context));
        loadProcesses.add(new RegisterBlockFamilies(context));
        loadProcesses.add(new PrepareWorld(context));
    }

    private void initHost() {
        loadProcesses.add(new RegisterMods(context, gameManifest));
        if (!headless) {
            loadProcesses.add(new InitialiseRendering(context));
        }
        loadProcesses.add(new InitialiseEntitySystem(context));
        loadProcesses.add(new RegisterBlocks(context, gameManifest));
        if (!headless) {
            loadProcesses.add(new InitialiseGraphics(context));
        }
        loadProcesses.add(new LoadPrefabs(context));
        loadProcesses.add(new ProcessBlockPrefabs(context));
        loadProcesses.add(new InitialiseComponentSystemManager(context));
        if (!headless) {
            loadProcesses.add(new RegisterInputSystem(context));
        }
        loadProcesses.add(new RegisterSystems(context, netMode));
        loadProcesses.add(new InitialiseCommandSystem(context));
        loadProcesses.add(new LoadExtraBlockData(context));
        loadProcesses.add(new InitialiseWorld(gameManifest, context));
        loadProcesses.add(new RegisterBlockFamilies(context));
        loadProcesses.add(new EnsureSaveGameConsistency(context));
        loadProcesses.add(new InitialisePhysics(context));
        loadProcesses.add(new InitialiseSystems(context));
        loadProcesses.add(new PreBeginSystems(context));
        loadProcesses.add(new LoadEntities(context));
        loadProcesses.add(new InitialiseBlockTypeEntities(context));
        loadProcesses.add(new CreateWorldEntity(context, gameManifest));
        loadProcesses.add(new InitialiseWorldGenerator(context));
        loadProcesses.add(new InitialiseRecordAndReplay(context));
        if (netMode.isServer()) {
            boolean dedicated;
            if (netMode == NetworkMode.DEDICATED_SERVER) {
                dedicated = true;
            } else if (netMode == NetworkMode.LISTEN_SERVER) {
                dedicated = false;
            } else {
                throw new IllegalStateException("Invalid server mode: " + netMode);
            }
            loadProcesses.add(new StartServer(context, dedicated));
        }
        loadProcesses.add(new PostBeginSystems(context));
        if (netMode.hasLocalClient()) {
            loadProcesses.add(new SetupLocalPlayer(context));
            loadProcesses.add(new AwaitCharacterSpawn(context));
        }
        loadProcesses.add(new PrepareWorld(context));
    }

    private void popStep() {
        if (current != null) {
            progress += current.getExpectedCost();
        }
        current = null;
        if (!loadProcesses.isEmpty()) {
            current = loadProcesses.remove();
            logger.debug("{}", current.getMessage()); //NOPMD
            current.begin();
        }
    }

    @Override
    public void dispose(boolean shuttingDown) {
        EngineTime time = (EngineTime) context.get(Time.class);
        time.setPaused(false);
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
        GameEngine gameEngine = context.get(GameEngine.class);
        EngineTime time = (EngineTime) context.get(Time.class);
        long startTime = time.getRealTimeInMs();
        while (current != null && time.getRealTimeInMs() - startTime < 20 && !gameEngine.hasPendingState()) {
            try {
                if (current.step()) {
                    popStep();
                }
            } catch (Exception e) {
                logger.error("Error while loading {}", current, e);
                String errorMessage = String.format("Failed to load game. There was an error during \"%s\".",
                        current == null ? "the last part" : current.getMessage());
                gameEngine.changeState(new StateMainMenu(errorMessage));
                CrashReporter.report(e, LoggingContext.getLoggingPath());
                return;
            }
        }
        if (current == null) {
            if (nuiManager != null) {
                nuiManager.closeScreen(loadingScreen);
                nuiManager.setHUDVisible(true);
            }
            context.get(GameEngine.class).changeState(new StateIngame(gameManifest, context));
        } else {
            float progressValue = (progress + current.getExpectedCost() * current.getProgress()) / maxProgress;
            if (nuiManager != null) {
                loadingScreen.updateStatus(current.getMessage(), progressValue);
                nuiManager.update(delta);
            }
            // chunk generation begins at the AwaitCharacterSpawn step
            if (current instanceof AwaitCharacterSpawn && !chunkGenerationStarted) {
                chunkGenerationStarted = true;
                // in case no chunks generate, this should be set for a basis
                timeLastChunkGenerated = time.getRealTimeInMs();
            }

            if (chunkGenerationStarted) {
                long timeSinceLastChunk = time.getRealTimeInMs() - timeLastChunkGenerated;
                long chunkGenerationTimeout = systemConfig.chunkGenerationFailTimeoutInMs.get();
                if (timeSinceLastChunk > chunkGenerationTimeout) {
                    String errorMessage = "World generation timed out, check the log for more info";
                    gameEngine.changeState(new StateMainMenu(errorMessage));
                }
            }
        }
    }

    @Override
    public void render() {
        if (nuiManager != null) {
            nuiManager.render();
        }
    }

    @Override
    public boolean isHibernationAllowed() {
        return false;
    }

    @Override
    public String getLoggingPhase() {
        return gameManifest.getTitle();
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void onChunkLoaded(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        EngineTime time = (EngineTime) context.get(Time.class);
        timeLastChunkGenerated = time.getRealTimeInMs();
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.EngineTime;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.modes.loadProcesses.AwaitCharacterSpawn;
import org.terasology.engine.modes.loadProcesses.CreateRemoteWorldEntity;
import org.terasology.engine.modes.loadProcesses.CreateWorldEntity;
import org.terasology.engine.modes.loadProcesses.EnsureSaveGameConsistency;
import org.terasology.engine.modes.loadProcesses.InitialiseBlockTypeEntities;
import org.terasology.engine.modes.loadProcesses.InitialiseCommandSystem;
import org.terasology.engine.modes.loadProcesses.InitialiseComponentSystemManager;
import org.terasology.engine.modes.loadProcesses.InitialiseEntitySystem;
import org.terasology.engine.modes.loadProcesses.InitialiseGraphics;
import org.terasology.engine.modes.loadProcesses.InitialisePhysics;
import org.terasology.engine.modes.loadProcesses.InitialiseRecordAndReplay;
import org.terasology.engine.modes.loadProcesses.InitialiseRemoteWorld;
import org.terasology.engine.modes.loadProcesses.InitialiseSystems;
import org.terasology.engine.modes.loadProcesses.InitialiseWorld;
import org.terasology.engine.modes.loadProcesses.InitialiseWorldGenerator;
import org.terasology.engine.modes.loadProcesses.JoinServer;
import org.terasology.engine.modes.loadProcesses.LoadEntities;
import org.terasology.engine.modes.loadProcesses.LoadExtraBlockData;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.modes.loadProcesses.PostBeginSystems;
import org.terasology.engine.modes.loadProcesses.PreBeginSystems;
import org.terasology.engine.modes.loadProcesses.PrepareWorld;
import org.terasology.engine.modes.loadProcesses.ProcessBlockPrefabs;
import org.terasology.engine.modes.loadProcesses.RegisterBlockFamilies;
import org.terasology.engine.modes.loadProcesses.RegisterBlocks;
import org.terasology.engine.modes.loadProcesses.RegisterInputSystem;
import org.terasology.engine.modes.loadProcesses.RegisterMods;
import org.terasology.engine.modes.loadProcesses.RegisterSystems;
import org.terasology.engine.modes.loadProcesses.SetupLocalPlayer;
import org.terasology.engine.modes.loadProcesses.SetupRemotePlayer;
import org.terasology.engine.modes.loadProcesses.StartServer;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkMode;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layers.mainMenu.loadingScreen.LoadingScreen;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.Queue;

public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);

    @In
    private ContextAwareClassFactory classFactory;
    @In
    private Config config;
    @In
    private EngineTime time;
    @In
    private Game game;
    @In
    private GameEngine gameEngine;


    private Context context;
    private final GameManifest gameManifest;
    private final NetworkMode netMode;
    private final Queue<Class<? extends LoadProcess>> loadProcesses = Queues.newArrayDeque();
    private LoadProcess current;
    private JoinStatus joinStatus;

    private NUIManager nuiManager;

    private LoadingScreen loadingScreen;

    private int progress;
    private int maxProgress;

    private boolean chunkGenerationStarted;
    private long timeLastChunkGenerated;

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
        updateContext(context);

        this.nuiManager = classFactory.createInjectableInstance(NUIManagerInternal.class, NUIManager.class);

        time.setPaused(true);
        time.setGameTime(gameManifest.getTime());

        game.load(gameManifest);

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
        for (Class<? extends LoadProcess> processClass : loadProcesses) {
            maxProgress += getExpectedCost(processClass);
        }
        context.put(NetworkMode.class, netMode);
        context.put(GameManifest.class, gameManifest);
        if (joinStatus != null) {
            context.put(JoinStatus.class, joinStatus);
        }

        popStep();
        loadingScreen = nuiManager.pushScreen("engine:loadingScreen", LoadingScreen.class);
        loadingScreen.updateStatus(current.getMessage(), current.getProgress());

        chunkGenerationStarted = false;
    }

    private void initClient() {
        loadProcesses.add(JoinServer.class);
        loadProcesses.add(InitialiseEntitySystem.class);
        loadProcesses.add(RegisterBlocks.class);
        loadProcesses.add(InitialiseGraphics.class);
        loadProcesses.add(LoadPrefabs.class);
        loadProcesses.add(ProcessBlockPrefabs.class);
        loadProcesses.add(LoadExtraBlockData.class);
        loadProcesses.add(InitialiseComponentSystemManager.class);
        loadProcesses.add(RegisterInputSystem.class);
        loadProcesses.add(RegisterSystems.class);
        loadProcesses.add(InitialiseCommandSystem.class);
        loadProcesses.add(InitialiseRemoteWorld.class);
        loadProcesses.add(InitialisePhysics.class);
        loadProcesses.add(InitialiseSystems.class);
        loadProcesses.add(PreBeginSystems.class);
        loadProcesses.add(CreateRemoteWorldEntity.class);
        loadProcesses.add(PostBeginSystems.class);
        loadProcesses.add(SetupRemotePlayer.class);
        loadProcesses.add(AwaitCharacterSpawn.class);
        loadProcesses.add(RegisterBlockFamilies.class);
        loadProcesses.add(PrepareWorld.class);
    }

    private void initHost() {
        loadProcesses.add(RegisterMods.class);
        loadProcesses.add(InitialiseEntitySystem.class);
        loadProcesses.add(RegisterBlocks.class);
        loadProcesses.add(InitialiseGraphics.class);
        loadProcesses.add(LoadPrefabs.class);
        loadProcesses.add(ProcessBlockPrefabs.class);
        loadProcesses.add(InitialiseComponentSystemManager.class);
        loadProcesses.add(RegisterInputSystem.class);
        loadProcesses.add(RegisterSystems.class);
        loadProcesses.add(InitialiseCommandSystem.class);
        loadProcesses.add(LoadExtraBlockData.class);
        loadProcesses.add(InitialiseWorld.class);
        loadProcesses.add(RegisterBlockFamilies.class);
        loadProcesses.add(EnsureSaveGameConsistency.class);
        loadProcesses.add(InitialisePhysics.class);
        loadProcesses.add(InitialiseSystems.class);
        loadProcesses.add(PreBeginSystems.class);
        loadProcesses.add(LoadEntities.class);
        loadProcesses.add(InitialiseBlockTypeEntities.class);
        loadProcesses.add(CreateWorldEntity.class);
        loadProcesses.add(InitialiseWorldGenerator.class);
        loadProcesses.add(InitialiseRecordAndReplay.class);
        if (netMode.isServer()) {
            loadProcesses.add(StartServer.class);
        }
        loadProcesses.add(PostBeginSystems.class);
        if (netMode.hasLocalClient()) {
            loadProcesses.add(SetupLocalPlayer.class);
            loadProcesses.add(AwaitCharacterSpawn.class);
        }
        loadProcesses.add(PrepareWorld.class);
    }

    private void popStep() {
        if (current != null) {
            progress += getExpectedCost(current.getClass());
        }
        current = null;
        if (!loadProcesses.isEmpty()) {
            Class<? extends LoadProcess> nextProcesses = loadProcesses.remove();
            current = classFactory.createWithContext(nextProcesses);
            logger.debug(current.getMessage());
            current.begin();
        }
    }

    @Override
    public void dispose(boolean shuttingDown) {
        time.setPaused(false);
    }

    @Override
    public void handleInput(float delta) {
        // Loading not handle any input. Relax and watch progress.
    }

    @Override
    public void update(float delta) {
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
            nuiManager.closeScreen(loadingScreen);
            nuiManager.setHUDVisible(true);
            gameEngine.changeState(new StateIngame(gameManifest, context));
        } else {
            float progressValue =
                    (progress + getExpectedCost(current.getClass()) * current.getProgress()) / maxProgress;
            loadingScreen.updateStatus(current.getMessage(), progressValue);
            nuiManager.update(delta);

            // chunk generation begins at the AwaitCharacterSpawn step
            if (current instanceof AwaitCharacterSpawn && !chunkGenerationStarted) {
                chunkGenerationStarted = true;
                // in case no chunks generate, this should be set for a basis
                timeLastChunkGenerated = time.getRealTimeInMs();
            }

            if (chunkGenerationStarted) {
                long timeSinceLastChunk = time.getRealTimeInMs() - timeLastChunkGenerated;
                long chunkGenerationTimeout = config.getSystem().getChunkGenerationFailTimeoutInMs();
                if (timeSinceLastChunk > chunkGenerationTimeout) {
                    String errorMessage = "World generation timed out, check the log for more info";
                    gameEngine.changeState(new StateMainMenu(errorMessage));
                }
            }
        }
    }

    @Override
    public void render() {
        nuiManager.render();
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
        timeLastChunkGenerated = time.getRealTimeInMs();
    }

    private int getExpectedCost(Class<? extends LoadProcess> process) {
        ExpectedCost expectedCost = process.getAnnotation(ExpectedCost.class);
        return expectedCost != null ? expectedCost.value() : 0;
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

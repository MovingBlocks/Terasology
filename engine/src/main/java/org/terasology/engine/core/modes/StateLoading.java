// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.loadProcesses.AwaitCharacterSpawn;
import org.terasology.engine.core.modes.loadProcesses.ConfigureEntitySystem;
import org.terasology.engine.core.modes.loadProcesses.CreateRemoteWorldEntity;
import org.terasology.engine.core.modes.loadProcesses.CreateWorldEntity;
import org.terasology.engine.core.modes.loadProcesses.InitialiseBlockTypeEntities;
import org.terasology.engine.core.modes.loadProcesses.InitialiseBlocks;
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
import org.terasology.engine.core.modes.loadProcesses.RegisterRemoteWorldSystems;
import org.terasology.engine.core.modes.loadProcesses.RegisterSystems;
import org.terasology.engine.core.modes.loadProcesses.RegisterWorldSystems;
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
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.nui.canvas.CanvasRenderer;

import java.util.Queue;

public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);
    private final GameManifest gameManifest;
    private final NetworkMode netMode;
    private final Queue<LoadProcess> loadProcesses = Queues.newArrayDeque();
    private Context context;
    private ServiceRegistry serviceRegistry;
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
        this.serviceRegistry = new ServiceRegistry();

        headless = context.get(DisplayDevice.class).isHeadless();
        
        CoreRegistry.setContext(context);
        context.getValue(NetworkSystem.class).setContext(context);
        systemConfig = context.get(SystemConfig.class);

        if (!headless) {
            // Assign a temporary NUIManager instance for showing the loading screen.
            this.nuiManager = new NUIManagerInternal((TerasologyCanvasRenderer) context.get(CanvasRenderer.class), context);
            context.put(NUIManager.class, this.nuiManager);
            // This temporary instance will be replaced by the one in the context when it is created.
            serviceRegistry.with(NUIManager.class).lifetime(Lifetime.Singleton).use(NUIManagerInternal.class);
        }

        EngineTime time = (EngineTime) context.get(Time.class);
        time.setPaused(true);
        time.setGameTime(gameManifest.getTime());

        context.get(Game.class).load(gameManifest);
        switch (netMode) {
            case CLIENT:
                initClient(engine, serviceRegistry);
                break;
            default:
                initHost(engine, serviceRegistry);
                break;
        }

        progress = 0;

        popStep();
        if (nuiManager != null) {
            loadingScreen = nuiManager.pushScreen("engine:loadingScreen", LoadingScreen.class);
            loadingScreen.updateStatus(current.getMessage(), current.getProgress());
        }
        chunkGenerationStarted = false;
    }

    private void initClient(GameEngine engine, ServiceRegistry serviceRegistry) {
        addAndTrack(new JoinServer(context, gameManifest, joinStatus));
        if (!headless) {
            addAndTrack(new InitialiseRendering(serviceRegistry));
        }
        addAndTrack(new InitialiseEntitySystem(context, serviceRegistry));
        addAndTrack(new RegisterBlocks(context, serviceRegistry));
        if (!headless) {
            addAndTrack(new InitialiseGraphics(context));
        }
//        addAndTrack(new LoadPrefabs(context));
//        addAndTrack(new ProcessBlockPrefabs(context));
        addAndTrack(new LoadExtraBlockData(serviceRegistry));
        addAndTrack(new InitialiseComponentSystemManager(serviceRegistry));
//        addAndTrack(new RegisterSystems(context, netMode));
//        addAndTrack(new RegisterWorldSystems(gameManifest, context));
        addAndTrack(new InitialiseCommandSystem(serviceRegistry));
        addAndTrack(new InitialiseRemoteWorld(context, serviceRegistry, gameManifest));
        addAndTrack(new InitialisePhysics(context, serviceRegistry));
//        addAndTrack(new InitialiseSystems(context));
//        addAndTrack(new PreBeginSystems(context));
//        addAndTrack(new CreateRemoteWorldEntity(context));
//        addAndTrack(new PostBeginSystems(context));
//        addAndTrack(new SetupRemotePlayer(context));
//        addAndTrack(new AwaitCharacterSpawn(context));
//        addAndTrack(new RegisterBlockFamilies(context));
//        addAndTrack(new PrepareWorld(context));
        addAndTrack(new SwitchToContextStep(engine));
        addAndTrack(new AddClientPostLoadProcessesStep());
    }

    private void initHost(GameEngine engine, ServiceRegistry serviceRegistry) {
        addAndTrack(new RegisterMods(context, serviceRegistry, gameManifest));
        if (!headless) {
            addAndTrack(new InitialiseRendering(serviceRegistry));
        }
        addAndTrack(new InitialiseEntitySystem(context, serviceRegistry));
        addAndTrack(new RegisterBlocks(context, serviceRegistry));
        if (!headless) {
            addAndTrack(new InitialiseGraphics(context));
        }
        addAndTrack(new InitialiseComponentSystemManager(serviceRegistry));
        addAndTrack(new InitialiseCommandSystem(serviceRegistry));
        addAndTrack(new LoadExtraBlockData(serviceRegistry));
        addAndTrack(new InitialiseWorld(gameManifest, context, serviceRegistry));
        addAndTrack(new InitialisePhysics(context, serviceRegistry));
        addAndTrack(new SwitchToContextStep(engine));
        // Post-Init processes
        addAndTrack(new AddHostPostLoadProcessesStep());
    }

    private void addAndTrack(LoadProcess process) {
        loadProcesses.add(process);
        maxProgress += process.getExpectedCost();
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

    private class SwitchToContextStep implements LoadProcess {
        private final GameEngine engine;

        SwitchToContextStep(GameEngine engine) {
            this.engine = engine;
        }

        @Override
        public String getMessage() {
            return "Entering new game context...";
        }

        @Override
        public boolean step() {
            context = engine.createChildContext(serviceRegistry);
            CoreRegistry.setContext(context);
            nuiManager = context.get(NUIManager.class);
            // Re-push the loading screen onto the new NUI manager so it stays
            // visible after the context swap. The old manager that created it
            // is no longer being rendered.
            if (nuiManager != null && loadingScreen != null) {
                loadingScreen = nuiManager.pushScreen("engine:loadingScreen", LoadingScreen.class);
            }
            return true;
        }

        @Override
        public void begin() {
        }

        @Override
        public float getProgress() {
            return 0;
        }

        @Override
        public int getExpectedCost() {
            return 0;
        }
    }

    private class AddHostPostLoadProcessesStep implements LoadProcess {
        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public boolean step() {
            addAndTrack(new ConfigureEntitySystem(context));
            addAndTrack(new InitialiseBlocks(gameManifest, context));
            addAndTrack(new LoadPrefabs(context));
            addAndTrack(new RegisterSystems(context, netMode));
            if (!headless) {
                addAndTrack(new RegisterInputSystem(context));
            }
            addAndTrack(new RegisterWorldSystems(gameManifest, context));
            addAndTrack(new RegisterBlockFamilies(context));
            addAndTrack(new ProcessBlockPrefabs(context));
            addAndTrack(new InitialiseSystems(context));
            addAndTrack(new PreBeginSystems(context));
            addAndTrack(new LoadEntities(context));
            addAndTrack(new InitialiseBlockTypeEntities(context));
            addAndTrack(new CreateWorldEntity(context, gameManifest));
            addAndTrack(new InitialiseWorldGenerator(context));
            addAndTrack(new InitialiseRecordAndReplay(context));
            if (netMode.isServer()) {
                boolean dedicated;
                if (netMode == NetworkMode.DEDICATED_SERVER) {
                    dedicated = true;
                } else if (netMode == NetworkMode.LISTEN_SERVER) {
                    dedicated = false;
                } else {
                    throw new IllegalStateException("Invalid server mode: " + netMode);
                }
                addAndTrack(new StartServer(context, dedicated));
            }
            addAndTrack(new PostBeginSystems(context));
            if (netMode.hasLocalClient()) {
                addAndTrack(new SetupLocalPlayer(context));
                addAndTrack(new AwaitCharacterSpawn(context));
            }
            addAndTrack(new PrepareWorld(context));
            return true;
        }

        @Override
        public void begin() {
        }

        @Override
        public float getProgress() {
            return 0;
        }

        @Override
        public int getExpectedCost() {
            return 0;
        }
    }

    private class AddClientPostLoadProcessesStep implements LoadProcess {
        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public boolean step() {
            addAndTrack(new ConfigureEntitySystem(context));
            addAndTrack(new InitialiseBlocks(gameManifest, context));
            addAndTrack(new LoadPrefabs(context));
            addAndTrack(new RegisterSystems(context, netMode));
            if (!headless) {
                addAndTrack(new RegisterInputSystem(context));
            }
            addAndTrack(new RegisterRemoteWorldSystems(context));
            addAndTrack(new RegisterBlockFamilies(context));
            addAndTrack(new ProcessBlockPrefabs(context));
            addAndTrack(new InitialiseSystems(context));
            addAndTrack(new PreBeginSystems(context));
            addAndTrack(new CreateRemoteWorldEntity(context));
            addAndTrack(new InitialiseBlockTypeEntities(context));
            addAndTrack(new InitialiseRecordAndReplay(context));
            if (netMode.isServer()) {
                boolean dedicated;
                if (netMode == NetworkMode.DEDICATED_SERVER) {
                    dedicated = true;
                } else if (netMode == NetworkMode.LISTEN_SERVER) {
                    dedicated = false;
                } else {
                    throw new IllegalStateException("Invalid server mode: " + netMode);
                }
                addAndTrack(new StartServer(context, dedicated));
            }
            addAndTrack(new PostBeginSystems(context));
            addAndTrack(new SetupRemotePlayer(context));
            addAndTrack(new AwaitCharacterSpawn(context));
            addAndTrack(new PrepareWorld(context));
            return true;
        }

        @Override
        public void begin() {
        }

        @Override
        public float getProgress() {
            return 0;
        }

        @Override
        public int getExpectedCost() {
            return 0;
        }
    }
}

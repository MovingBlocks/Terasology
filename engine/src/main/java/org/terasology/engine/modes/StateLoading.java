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

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.EngineTime;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.engine.modes.loadProcesses.AwaitCharacterSpawn;
import org.terasology.engine.modes.loadProcesses.CacheBlocks;
import org.terasology.engine.modes.loadProcesses.CacheTextures;
import org.terasology.engine.modes.loadProcesses.CreateRemoteWorldEntity;
import org.terasology.engine.modes.loadProcesses.CreateWorldEntity;
import org.terasology.engine.modes.loadProcesses.EnsureSaveGameConsistency;
import org.terasology.engine.modes.loadProcesses.InitialiseBlockTypeEntities;
import org.terasology.engine.modes.loadProcesses.InitialiseCommandSystem;
import org.terasology.engine.modes.loadProcesses.InitialiseComponentSystemManager;
import org.terasology.engine.modes.loadProcesses.InitialiseEntitySystem;
import org.terasology.engine.modes.loadProcesses.InitialiseGraphics;
import org.terasology.engine.modes.loadProcesses.InitialisePhysics;
import org.terasology.engine.modes.loadProcesses.InitialiseRemoteWorld;
import org.terasology.engine.modes.loadProcesses.InitialiseSystems;
import org.terasology.engine.modes.loadProcesses.InitialiseWorld;
import org.terasology.engine.modes.loadProcesses.InitialiseWorldGenerator;
import org.terasology.engine.modes.loadProcesses.JoinServer;
import org.terasology.engine.modes.loadProcesses.LoadEntities;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.modes.loadProcesses.PostBeginSystems;
import org.terasology.engine.modes.loadProcesses.PreBeginSystems;
import org.terasology.engine.modes.loadProcesses.PrepareWorld;
import org.terasology.engine.modes.loadProcesses.ProcessBlockPrefabs;
import org.terasology.engine.modes.loadProcesses.RegisterBiomes;
import org.terasology.engine.modes.loadProcesses.RegisterBlocks;
import org.terasology.engine.modes.loadProcesses.RegisterInputSystem;
import org.terasology.engine.modes.loadProcesses.RegisterMods;
import org.terasology.engine.modes.loadProcesses.RegisterSystems;
import org.terasology.engine.modes.loadProcesses.SetupLocalPlayer;
import org.terasology.engine.modes.loadProcesses.SetupRemotePlayer;
import org.terasology.engine.modes.loadProcesses.StartServer;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkMode;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.CanvasRenderer;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layers.mainMenu.loadingScreen.LoadingScreen;

import java.util.Queue;

/**
 */
public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);

    private Context context;
    private GameManifest gameManifest;
    private NetworkMode netMode;
    private Queue<LoadProcess> loadProcesses = Queues.newArrayDeque();
    private LoadProcess current;
    private JoinStatus joinStatus;

    private NUIManager nuiManager;

    private LoadingScreen loadingScreen;

    private int progress;
    private int maxProgress;

    /**
     * Constructor for server or single player games
     *
     * @param gameManifest
     * @param netMode
     */
    public StateLoading(GameManifest gameManifest, NetworkMode netMode) {
        Preconditions.checkArgument(netMode != NetworkMode.CLIENT);

        this.gameManifest = gameManifest;
        this.netMode = netMode;
    }

    /**
     * Constructor for client of multiplayer game
     *
     * @param joinStatus
     */
    public StateLoading(JoinStatus joinStatus) {
        this.gameManifest = new GameManifest();
        this.netMode = NetworkMode.CLIENT;
        this.joinStatus = joinStatus;
    }

    @Override
    public void init(GameEngine engine) {
        this.context = engine.createChildContext();
        CoreRegistry.setContext(context);

        this.nuiManager = new NUIManagerInternal(context.get(CanvasRenderer.class), context);
        context.put(NUIManager.class, nuiManager);

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
        loadingScreen = nuiManager.pushScreen("engine:loadingScreen", LoadingScreen.class);
        loadingScreen.updateStatus(current.getMessage(), current.getProgress());
    }

    private void initClient() {
        loadProcesses.add(new JoinServer(context, gameManifest, joinStatus));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new InitialiseEntitySystem(context));
        loadProcesses.add(new RegisterBlocks(context, gameManifest));
        loadProcesses.add(new RegisterBiomes(context, gameManifest));
        loadProcesses.add(new InitialiseGraphics(context));
        loadProcesses.add(new CacheBlocks(context));
        loadProcesses.add(new LoadPrefabs(context));
        loadProcesses.add(new ProcessBlockPrefabs(context));
        loadProcesses.add(new InitialiseComponentSystemManager(context));
        loadProcesses.add(new RegisterInputSystem(context));
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
        loadProcesses.add(new PrepareWorld(context));
    }

    private void initHost() {
        loadProcesses.add(new RegisterMods(context, gameManifest));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new InitialiseEntitySystem(context));
        loadProcesses.add(new RegisterBlocks(context, gameManifest));
        loadProcesses.add(new RegisterBiomes(context, gameManifest));
        loadProcesses.add(new InitialiseGraphics(context));
        loadProcesses.add(new CacheBlocks(context));
        loadProcesses.add(new LoadPrefabs(context));
        loadProcesses.add(new ProcessBlockPrefabs(context));
        loadProcesses.add(new InitialiseComponentSystemManager(context));
        loadProcesses.add(new RegisterInputSystem(context));
        loadProcesses.add(new RegisterSystems(context, netMode));
        loadProcesses.add(new InitialiseCommandSystem(context));
        loadProcesses.add(new InitialiseWorld(gameManifest, context));
        loadProcesses.add(new EnsureSaveGameConsistency(context));
        loadProcesses.add(new InitialisePhysics(context));
        loadProcesses.add(new InitialiseSystems(context));
        loadProcesses.add(new PreBeginSystems(context));
        loadProcesses.add(new LoadEntities(context));
        loadProcesses.add(new InitialiseBlockTypeEntities(context));
        loadProcesses.add(new CreateWorldEntity(context));
        loadProcesses.add(new InitialiseWorldGenerator(context));
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
            logger.debug(current.getMessage());
            current.begin();
        }
    }

    @Override
    public void dispose() {
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
            if (current.step()) {
                popStep();
            }
        }
        if (current == null) {
            nuiManager.closeScreen(loadingScreen);
            nuiManager.setHUDVisible(true);
            context.get(GameEngine.class).changeState(new StateIngame(gameManifest, context));
        } else {
            float progressValue = (progress + current.getExpectedCost() * current.getProgress()) / maxProgress;
            loadingScreen.updateStatus(current.getMessage(), progressValue);
            nuiManager.update(delta);
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
}

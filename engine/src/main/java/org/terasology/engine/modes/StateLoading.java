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
import org.terasology.engine.EngineTime;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.engine.modes.loadProcesses.AwaitCharacterSpawn;
import org.terasology.engine.modes.loadProcesses.CacheBlocks;
import org.terasology.engine.modes.loadProcesses.CacheTextures;
import org.terasology.engine.modes.loadProcesses.CreateWorldEntity;
import org.terasology.engine.modes.loadProcesses.InitialiseBlockTypeEntities;
import org.terasology.engine.modes.loadProcesses.InitialiseCommandSystem;
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
import org.terasology.engine.modes.loadProcesses.RegisterBlockFamilyFactories;
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
import org.terasology.rendering.nui.layers.mainMenu.loadingScreen.LoadingScreen;

import java.util.Queue;

/**
 * @author Immortius
 */
public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);

    private GameManifest gameManifest;
    private String serverAddress;
    private int serverPort;
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
        this.nuiManager = CoreRegistry.get(NUIManager.class);
        this.netMode = netMode;
    }

    /**
     * Constructor for client of multiplayer game
     *
     * @param joinStatus
     */
    public StateLoading(JoinStatus joinStatus) {
        this.gameManifest = new GameManifest();
        this.nuiManager = CoreRegistry.get(NUIManager.class);
        this.netMode = NetworkMode.CLIENT;
        this.joinStatus = joinStatus;
    }

    @Override
    public void init(GameEngine engine) {
        EngineTime time = (EngineTime) CoreRegistry.get(Time.class);
        time.setPaused(true);
        time.setGameTime(0);

        CoreRegistry.get(Game.class).load(gameManifest);
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
        loadProcesses.add(new JoinServer(gameManifest, joinStatus));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new RegisterBlockFamilyFactories());
        loadProcesses.add(new RegisterBlocks(gameManifest));
        loadProcesses.add(new CacheBlocks());
        loadProcesses.add(new InitialiseGraphics());
        loadProcesses.add(new InitialiseEntitySystem());
        loadProcesses.add(new LoadPrefabs());
        loadProcesses.add(new ProcessBlockPrefabs());
        loadProcesses.add(new RegisterInputSystem());
        loadProcesses.add(new RegisterSystems(netMode));
        loadProcesses.add(new InitialiseCommandSystem());
        loadProcesses.add(new InitialiseRemoteWorld(gameManifest));
        loadProcesses.add(new InitialisePhysics());
        loadProcesses.add(new InitialiseSystems());
        loadProcesses.add(new PreBeginSystems());
        loadProcesses.add(new PostBeginSystems());
        loadProcesses.add(new SetupRemotePlayer());
        loadProcesses.add(new AwaitCharacterSpawn());
        loadProcesses.add(new PrepareWorld());
    }

    private void initHost() {
        loadProcesses.add(new RegisterMods(gameManifest));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new RegisterBlockFamilyFactories());
        loadProcesses.add(new RegisterBlocks(gameManifest));
        loadProcesses.add(new CacheBlocks());
        loadProcesses.add(new InitialiseGraphics());
        loadProcesses.add(new InitialiseEntitySystem());
        loadProcesses.add(new LoadPrefabs());
        loadProcesses.add(new ProcessBlockPrefabs());
        loadProcesses.add(new RegisterInputSystem());
        loadProcesses.add(new RegisterSystems(netMode));
        loadProcesses.add(new InitialiseCommandSystem());
        loadProcesses.add(new InitialiseWorld(gameManifest));
        loadProcesses.add(new InitialisePhysics());
        loadProcesses.add(new InitialiseSystems());
        loadProcesses.add(new LoadEntities());
        loadProcesses.add(new PreBeginSystems());
        loadProcesses.add(new InitialiseBlockTypeEntities());
        loadProcesses.add(new CreateWorldEntity());
        loadProcesses.add(new InitialiseWorldGenerator(gameManifest));
        if (netMode == NetworkMode.SERVER) {
            loadProcesses.add(new StartServer());
        }
        loadProcesses.add(new PostBeginSystems());
        loadProcesses.add(new SetupLocalPlayer());
        loadProcesses.add(new AwaitCharacterSpawn());
        loadProcesses.add(new PrepareWorld());
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
        EngineTime time = (EngineTime) CoreRegistry.get(Time.class);
        time.setPaused(false);
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
        EngineTime time = (EngineTime) CoreRegistry.get(Time.class);
        long startTime = time.getRawTimeInMs();
        while (current != null && time.getRawTimeInMs() - startTime < 20) {
            if (current.step()) {
                popStep();
            }
        }
        if (current == null) {
            nuiManager.closeScreen(loadingScreen);
            nuiManager.setHUDVisible(true);
            CoreRegistry.get(GameEngine.class).changeState(new StateIngame());
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

}

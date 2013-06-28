/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Queues;
import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
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
import org.terasology.engine.modes.loadProcesses.InitialiseRemoteWorld;
import org.terasology.engine.modes.loadProcesses.InitialiseSystems;
import org.terasology.engine.modes.loadProcesses.InitialiseWorld;
import org.terasology.engine.modes.loadProcesses.JoinServer;
import org.terasology.engine.modes.loadProcesses.LoadEntities;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
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
import org.terasology.logic.manager.GUIManager;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.gui.windows.UIScreenLoading;
import org.terasology.world.WorldInfo;

import java.util.Queue;

/**
 * @author Immortius
 */
public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);

    private WorldInfo worldInfo;
    private String serverAddress;
    private int serverPort;
    private NetworkMode netMode;
    private Queue<LoadProcess> loadProcesses = Queues.newArrayDeque();
    private LoadProcess current;
    private int currentExpectedSteps = 0;
    private int completedSteps = 0;

    private GUIManager guiManager;

    private UIScreenLoading loadingScreen;

    /**
     * Constructor for server or single player games
     *
     * @param worldInfo
     * @param netMode
     */
    public StateLoading(WorldInfo worldInfo, NetworkMode netMode) {
        this.worldInfo = worldInfo;
        this.guiManager = CoreRegistry.get(GUIManager.class);
        this.netMode = netMode;
    }

    /**
     * Constructor for client of multiplayer game
     *
     * @param netMode
     */
    public StateLoading(NetworkMode netMode) {
        this.worldInfo = new WorldInfo();
        this.guiManager = CoreRegistry.get(GUIManager.class);
        this.netMode = netMode;
    }

    @Override
    public void init(GameEngine engine) {
        ((EngineTime) CoreRegistry.get(Time.class)).setGameTime(0);
        switch (netMode) {
            case CLIENT:
                initClient();
                break;
            default:
                initHost();
                break;
        }

        popStep();
        GUIManager guiManager = CoreRegistry.get(GUIManager.class);
        guiManager.closeAllWindows();
        loadingScreen = (UIScreenLoading) guiManager.openWindow("loading");
        loadingScreen.updateStatus(current.getMessage(), completedSteps / (currentExpectedSteps * 100f));
    }

    private void initClient() {
        loadProcesses.add(new JoinServer(worldInfo));
        loadProcesses.add(new RegisterMods(worldInfo));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new RegisterBlockFamilyFactories());
        loadProcesses.add(new RegisterBlocks(worldInfo));
        loadProcesses.add(new CacheBlocks());
        loadProcesses.add(new InitialiseEntitySystem());
        loadProcesses.add(new LoadPrefabs());
        loadProcesses.add(new ProcessBlockPrefabs());
        loadProcesses.add(new RegisterInputSystem());
        loadProcesses.add(new RegisterSystems(netMode));
        loadProcesses.add(new InitialiseCommandSystem());
        loadProcesses.add(new InitialiseRemoteWorld(worldInfo));
        loadProcesses.add(new InitialiseSystems());
        loadProcesses.add(new SetupRemotePlayer());
        loadProcesses.add(new AwaitCharacterSpawn());
        loadProcesses.add(new PrepareWorld());
    }

    private void initHost() {
        loadProcesses.add(new RegisterMods(worldInfo));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new RegisterBlockFamilyFactories());
        loadProcesses.add(new RegisterBlocks(worldInfo));
        loadProcesses.add(new CacheBlocks());
        loadProcesses.add(new InitialiseEntitySystem());
        loadProcesses.add(new LoadPrefabs());
        loadProcesses.add(new ProcessBlockPrefabs());
        loadProcesses.add(new RegisterInputSystem());
        loadProcesses.add(new RegisterSystems(netMode));
        loadProcesses.add(new InitialiseCommandSystem());
        loadProcesses.add(new InitialiseWorld(worldInfo));
        loadProcesses.add(new InitialiseSystems());
        loadProcesses.add(new LoadEntities(worldInfo));
        loadProcesses.add(new InitialiseBlockTypeEntities());
        loadProcesses.add(new CreateWorldEntity());
        if (netMode == NetworkMode.SERVER) {
            loadProcesses.add(new StartServer());
        }
        loadProcesses.add(new SetupLocalPlayer());
        loadProcesses.add(new AwaitCharacterSpawn());
        loadProcesses.add(new PrepareWorld());
    }

    private void popStep() {
        current = null;
        currentExpectedSteps = 0;
        while (currentExpectedSteps == 0 && !loadProcesses.isEmpty()) {
            current = loadProcesses.remove();
            logger.debug(current.getMessage());
            currentExpectedSteps = current.begin();
        }
        completedSteps = 0;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
        long startTime = 1000 * Sys.getTime() / Sys.getTimerResolution();

        while (current != null && 1000 * Sys.getTime() / Sys.getTimerResolution() - startTime < 20) {
            if (current.step()) {
                popStep();
            } else {
                completedSteps++;
            }
        }
        if (current == null) {
            CoreRegistry.get(GUIManager.class).closeWindow("loading");
            CoreRegistry.get(GameEngine.class).changeState(new StateIngame());
        } else {
            if (currentExpectedSteps > 0) {
                loadingScreen.updateStatus(current.getMessage(), 100f * completedSteps / currentExpectedSteps);
            } else {
                loadingScreen.updateStatus(current.getMessage(), 0);
            }
            guiManager.update();
        }
    }

    @Override
    public void render() {
        CoreRegistry.get(GUIManager.class).render();
    }

    @Override
    public boolean isHibernationAllowed() {
        return false;
    }

}

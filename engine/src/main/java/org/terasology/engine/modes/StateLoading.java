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
import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.EngineTime;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.engine.modes.loadProcesses.*;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.logic.manager.GUIManager;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.gui.windows.UIScreenLoading;

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

    private GUIManager guiManager;

    private UIScreenLoading loadingScreen;

    /**
     * Constructor for server or single player games
     *
     * @param gameManifest
     * @param netMode
     */
    public StateLoading(GameManifest gameManifest, NetworkMode netMode) {
        Preconditions.checkArgument(netMode != NetworkMode.CLIENT);

        this.gameManifest = gameManifest;
        this.guiManager = CoreRegistry.get(GUIManager.class);
        this.netMode = netMode;
    }

    /**
     * Constructor for client of multiplayer game
     *
     * @param joinStatus
     */
    public StateLoading(JoinStatus joinStatus) {
        this.gameManifest = new GameManifest();
        this.guiManager = CoreRegistry.get(GUIManager.class);
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

        popStep();
        guiManager.closeAllWindows();
        loadingScreen = (UIScreenLoading) guiManager.openWindow("loading");
        loadingScreen.updateStatus(current.getMessage(), current.getProgress() * 100f);
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
        loadProcesses.add(new InitialiseSystems());
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
        loadProcesses.add(new InitialiseSystems());
        loadProcesses.add(new LoadEntities());
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
        long startTime = 1000 * Sys.getTime() / Sys.getTimerResolution();
        while (current != null && 1000 * Sys.getTime() / Sys.getTimerResolution() - startTime < 20) {
            if (current.step()) {
                popStep();
            }
        }
        if (current == null) {
            CoreRegistry.get(GUIManager.class).closeWindow("loading");
            CoreRegistry.get(GameEngine.class).changeState(new StateIngame());
        } else {
            loadingScreen.updateStatus(current.getMessage(), 100f * current.getProgress());
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

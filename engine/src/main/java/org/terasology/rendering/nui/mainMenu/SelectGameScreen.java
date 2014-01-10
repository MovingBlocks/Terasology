/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.systems.In;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIScreenUtil;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.ListEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIList;
import org.terasology.rendering.nui.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.mainMenu.savedGames.GameProvider;
import org.terasology.utilities.FilesUtil;

import java.nio.file.Path;

/**
 * @author Immortius
 */
public class SelectGameScreen extends UIScreen {

    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);

    @In
    private NUIManager nuiManager;

    private boolean loadingAsServer;

    @Override
    public void initialise() {
        final UIList<GameInfo> gameList = find("gameList", UIList.class);

        refreshList(gameList);
        gameList.subscribe(new ListEventListener<GameInfo>() {
            @Override
            public void onItemActivated(GameInfo item) {
                loadGame(item);
            }
        });

        UIScreenUtil.trySubscribe(this, "create", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                CreateGameScreen createGameScreen = (CreateGameScreen) nuiManager.pushScreen("engine:createGameScreen");
                createGameScreen.setLoadingAsServer(loadingAsServer);
            }
        });

        UIScreenUtil.trySubscribe(this, "load", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                GameInfo gameInfo = gameList.getSelection();
                if (gameInfo != null) {
                    loadGame(gameInfo);
                }
            }
        });

        UIScreenUtil.trySubscribe(this, "delete", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                GameInfo gameInfo = gameList.getSelection();
                if (gameInfo != null) {
                    Path world = PathManager.getInstance().getSavePath(gameInfo.getManifest().getTitle());
                    try {
                        FilesUtil.recursiveDelete(world);
                        gameList.getList().remove(gameInfo);
                        gameList.setSelection(null);
                    } catch (Exception e) {
                        logger.error("Failed to delete saved game", e);
                        // TODO: show error
                    }
                }
            }
        });

        UIScreenUtil.trySubscribe(this, "close", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.popScreen();
            }
        });
    }

    private void loadGame(GameInfo item) {
        try {
            GameManifest manifest = item.getManifest();
            Config config = CoreRegistry.get(Config.class);

            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(manifest.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, (loadingAsServer) ? NetworkMode.SERVER : NetworkMode.NONE));
        } catch (Exception e) {
            // TODO: Display error
            logger.error("Failed to load saved game", e);
        }
    }

    public boolean isLoadingAsServer() {
        return loadingAsServer;
    }

    public void setLoadingAsServer(boolean loadingAsServer) {
        this.loadingAsServer = loadingAsServer;
    }

    private void refreshList(UIList<GameInfo> gameList) {
        gameList.setList(GameProvider.getSavedGames());
    }

}

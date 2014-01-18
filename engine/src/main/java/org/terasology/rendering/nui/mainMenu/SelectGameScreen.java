/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.ItemActivateEventListener;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.utilities.FilesUtil;

import java.nio.file.Path;

/**
 * @author Immortius
 */
public class SelectGameScreen extends UIScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);

    @In
    private Config config;

    private boolean loadingAsServer;

    @Override
    public void initialise() {
        final UIList<GameInfo> gameList = find("gameList", UIList.class);

        refreshList(gameList);
        gameList.subscribe(new ItemActivateEventListener<GameInfo>() {
            @Override
            public void onItemActivated(UIWidget widget, GameInfo item) {
                loadGame(item);
            }
        });

        WidgetUtil.trySubscribe(this, "create", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                CreateGameScreen createGameScreen = getManager().pushScreen("engine:createGameScreen", CreateGameScreen.class);
                createGameScreen.setLoadingAsServer(loadingAsServer);
            }
        });

        WidgetUtil.trySubscribe(this, "load", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                GameInfo gameInfo = gameList.getSelection();
                if (gameInfo != null) {
                    loadGame(gameInfo);
                }
            }
        });

        WidgetUtil.trySubscribe(this, "delete", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                GameInfo gameInfo = gameList.getSelection();
                if (gameInfo != null) {
                    Path world = PathManager.getInstance().getSavePath(gameInfo.getManifest().getTitle());
                    try {
                        FilesUtil.recursiveDelete(world);
                        gameList.getList().remove(gameInfo);
                        gameList.setSelection(null);
                    } catch (Exception e) {
                        logger.error("Failed to delete saved game", e);
                        getManager().pushScreen("engine:errorMessagePopup", ErrorMessagePopup.class).setError("Error Deleting Game", e.getMessage());
                    }
                }
            }
        });

        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
    }

    private void loadGame(GameInfo item) {
        try {
            GameManifest manifest = item.getManifest();

            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(manifest.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, (loadingAsServer) ? NetworkMode.SERVER : NetworkMode.NONE));
        } catch (Exception e) {
            logger.error("Failed to load saved game", e);
            getManager().pushScreen("engine:errorMessagePopup", ErrorMessagePopup.class).setError("Error Loading Game", e.getMessage());
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

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
package org.terasology.rendering.nui.mainMenu.selectGame;

import com.google.common.collect.Maps;
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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author Immortius
 */
public class SelectGameScreen extends UIScreen {

    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @In
    private NUIManager nuiManager;

    private boolean loadingAsServer;

    @Override
    public void initialise() {
        UIList<GameInfo> gameList = find("gameList", UIList.class);

        refreshList(gameList);
        gameList.subscribe(new ListEventListener<GameInfo>() {
            @Override
            public void onItemActivated(GameInfo item) {
                try {
                    GameManifest manifest = item.manifest;
                    Config config = CoreRegistry.get(Config.class);

                    config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
                    config.getWorldGeneration().setWorldTitle(manifest.getTitle());
                    CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, (loadingAsServer) ? NetworkMode.SERVER : NetworkMode.NONE));
                } catch (Exception e) {
                    // TODO: Display error
                    logger.error("Failed to load saved game", e);
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

    public boolean isLoadingAsServer() {
        return loadingAsServer;
    }

    public void setLoadingAsServer(boolean loadingAsServer) {
        this.loadingAsServer = loadingAsServer;
    }

    private void refreshList(UIList<GameInfo> gameList) {
        gameList.getList().clear();
        Path savedGames = PathManager.getInstance().getSavesPath();
        SortedMap<FileTime, Path> savedGamePaths = Maps.newTreeMap(Collections.reverseOrder());
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(savedGames)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry.resolve(GameManifest.DEFAULT_FILE_NAME))) {
                    savedGamePaths.put(Files.getLastModifiedTime(entry), entry);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read saved games path", e);
        }

        for (Map.Entry<FileTime, Path> world : savedGamePaths.entrySet()) {
            Path gameManifest = world.getValue().resolve(GameManifest.DEFAULT_FILE_NAME);

            if (!Files.isRegularFile(gameManifest)) {
                continue;
            }
            try {
                GameManifest info = GameManifest.load(gameManifest);
                if (!info.getTitle().isEmpty()) {
                    Date date = new Date(world.getKey().toMillis());
                    gameList.getList().add(new GameInfo(info, date));
                }
            } catch (IOException e) {
                logger.error("Failed reading world data object.", e);
            }
        }
    }

    private static class GameInfo {
        private Date timestamp;
        private GameManifest manifest;

        public GameInfo(GameManifest manifest, Date timestamp) {
            this.manifest = manifest;
            this.timestamp = timestamp;
        }

        public String toString() {
            return manifest.getTitle() + "\n" + DATE_FORMAT.format(timestamp);
        }
    }
}

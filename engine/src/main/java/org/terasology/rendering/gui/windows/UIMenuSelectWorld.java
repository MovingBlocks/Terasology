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
package org.terasology.rendering.gui.windows;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.gui.dialogs.UIDialogCreateNewWorld;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.utilities.FilesUtil;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
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
 * Select world menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuSelectWorld extends UIWindow {
    private static final Logger logger = LoggerFactory.getLogger(UIMenuSelectWorld.class);

    final UIList list;
    final UIButton goToBack;
    final UIButton createNewWorld;
    final UIButton loadFromList;
    final UIButton deleteFromList;

    private boolean createServerGame;

    public UIMenuSelectWorld() {
        setId("selectworld");
        setBackgroundImage("engine:menubackground");
        setModal(true);
        maximize();

        list = new UIList();
        list.setSize(new Vector2f(512f, 256f));
        list.setPadding(new Vector4f(10f, 5f, 10f, 5f));
        list.setBackgroundImage("engine:gui_menu", new Vector2f(264f, 18f), new Vector2f(159f, 63f));
        list.setBorderImage("engine:gui_menu", new Vector2f(256f, 0f), new Vector2f(175f, 88f), new Vector4f(16f, 7f, 7f, 7f));
        list.addDoubleClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadSelectedWorld();
            }
        });
        list.setHorizontalAlign(EHorizontalAlign.CENTER);
        list.setPosition(new Vector2f(0f, 230f));
        list.setVisible(true);

        goToBack = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        goToBack.getLabel().setText("Back");
        goToBack.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("main");
            }
        });
        goToBack.setHorizontalAlign(EHorizontalAlign.CENTER);
        goToBack.setPosition(new Vector2f(0f, 600f));
        goToBack.setVisible(true);

        loadFromList = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        loadFromList.getLabel().setText("Load");
        loadFromList.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadSelectedWorld();
            }
        });
        loadFromList.setHorizontalAlign(EHorizontalAlign.CENTER);
        loadFromList.setPosition(new Vector2f(30f, 505f));
        loadFromList.setVisible(true);

        deleteFromList = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        deleteFromList.getLabel().setText("Delete");
        deleteFromList.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (list.getSelection() == null) {
                    getGUIManager().showMessage("Error", "Please choose a saved game first.");
                    return;
                }

                int selectionIndex = list.getSelectionIndex();
                if ( (selectionIndex < 0) || (selectionIndex >= list.getItemCount()) ) {
                    getGUIManager().showMessage("Error", "Please choose a saved game first.");
                    return;
                }

                try {
                    GameManifest gameManifest = (GameManifest) list.getSelection().getValue();
                    Path world = PathManager.getInstance().getSavePath(gameManifest.getTitle());
                    FilesUtil.recursiveDelete(world);
                    list.removeItem(selectionIndex);
                } catch (Exception e) {
                    logger.error("Failed to delete saved game", e);
                    getGUIManager().showMessage("Error", "Failed to remove saved game - sorry.");
                }
            }
        });
        deleteFromList.setHorizontalAlign(EHorizontalAlign.CENTER);
        deleteFromList.setPosition(new Vector2f(196f, 505f));
        deleteFromList.setVisible(true);

        createNewWorld = new UIButton(new Vector2f(192f, 32f), UIButton.ButtonType.NORMAL);
        createNewWorld.getLabel().setText("Create new world");
        createNewWorld.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                UIDialogCreateNewWorld dialog = new UIDialogCreateNewWorld(createServerGame);
                dialog.open();
            }
        });
        createNewWorld.setHorizontalAlign(EHorizontalAlign.CENTER);
        createNewWorld.setPosition(new Vector2f(-166f, 505f));
        createNewWorld.setVisible(true);

        fillList();

        addDisplayElement(list);
        addDisplayElement(loadFromList);
        addDisplayElement(goToBack);
        addDisplayElement(createNewWorld);
        addDisplayElement(deleteFromList);
    }

    private void loadSelectedWorld() {

        if (list.getItemCount() < 1) {
            getGUIManager().showMessage("Error", "You did not create a world yet!");
            return;
        }

        if (list.getSelection() == null) {
            getGUIManager().showMessage("Error", "Please choose a world!");
            return;
        }

        try {
            GameManifest info = (GameManifest) list.getSelection().getValue();
            Config config = CoreRegistry.get(Config.class);

            config.getWorldGeneration().setDefaultSeed(info.getSeed());
            config.getWorldGeneration().setWorldTitle(info.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(info, (createServerGame) ? NetworkMode.SERVER : NetworkMode.NONE));
        } catch (Exception e) {
            getGUIManager().showMessage("Error", "Failed reading saved game. Sorry.");
        }
    }

    public void fillList() {
        list.removeAll();

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

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Map.Entry<FileTime, Path> world : savedGamePaths.entrySet()) {
            Path gameManifest = world.getValue().resolve(GameManifest.DEFAULT_FILE_NAME);

            if (!Files.isRegularFile(gameManifest)) {
                continue;
            }
            try {
                GameManifest info = GameManifest.load(gameManifest);
                if (!info.getTitle().isEmpty()) {
                    Date date = new Date(world.getKey().toMillis());
                    UIListItem item = new UIListItem(info.getTitle() + "\n" + dateFormat.format(date), info);
                    item.setPadding(new Vector4f(10f, 5f, 10f, 5f));
                    list.addItem(item);
                }
            } catch (IOException e) {
                logger.error("Failed reading world data object.", e);
            }
        }
    }

    public int getWorldCount() {
        return list.getItemCount();
    }

    public void setCreateServerGame(boolean createServerGame) {
        this.createServerGame = createServerGame;
    }
}

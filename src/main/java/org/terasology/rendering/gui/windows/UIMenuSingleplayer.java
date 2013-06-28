/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateLoading;
import org.terasology.game.types.GameType;
import org.terasology.game.types.GameTypeManager;
import org.terasology.game.paths.PathManager;
import org.terasology.rendering.gui.dialogs.UIDialogCreateNewWorld;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldUtil;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Select world menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuSingleplayer extends UIWindow {
    private static final Logger logger = LoggerFactory.getLogger(UIMenuSingleplayer.class);

    final UIList list;
    final UIButton goToBack;
    final UIButton createNewWorld;
    final UIButton loadFromList;
    final UIButton deleteFromList;

    public UIMenuSingleplayer() {
        setId("singleplayer");
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
                    getGUIManager().showMessage("Error", "Please choose a world first.");
                    return;
                }

                try {
                    WorldInfo worldInfo = (WorldInfo) list.getSelection().getValue();
                    File world = PathManager.getInstance().getWorldSavePath(worldInfo.getTitle());
                    WorldUtil.deleteWorld(world);
                    list.removeItem(list.getSelectionIndex());
                } catch (Exception e) {
                    getGUIManager().showMessage("Error", "Failed deleting world data object. Sorry.");
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
                UIDialogCreateNewWorld dialog = new UIDialogCreateNewWorld();
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
            WorldInfo info = (WorldInfo) list.getSelection().getValue();

            Config config = CoreRegistry.get(Config.class);

            config.getWorldGeneration().setDefaultSeed(info.getSeed());
            config.getWorldGeneration().setWorldTitle(info.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(info));
        } catch (Exception e) {
            getGUIManager().showMessage("Error", "Failed reading world data object. Sorry.");
        }
    }

    public void fillList() {
        list.removeAll();
        File worldCatalog = PathManager.getInstance().getWorldPath();

        // Create a map that sorts valid saved worlds by their last modified date
        SortedMap<Date, File> savedWorlds = new TreeMap<Date, File>(Collections.reverseOrder());
        for (File f : worldCatalog.listFiles()) {

            // Ignore non-directories
            if (!f.isDirectory()) {
                continue;
            }

            // Ignore worlds without entity.dat - TODO: Find a file to date-test in a less fragile (hard coded) fashion?
            File entityDat = new File (f, "entity.dat");
            if (!entityDat.exists()) {
                continue;
            }

            // Map together the world directories with the modified timestamp from the entity file inside
            savedWorlds.put(new Date(entityDat.lastModified()), f);
        }

        // Format year-month-date to reduce day/month swap confusion
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Map.Entry<Date, File> world : savedWorlds.entrySet()) {

            File worldManifest = new File(world.getValue(), WorldInfo.DEFAULT_FILE_NAME);

            // Ignore worlds with no manifest
            if (!worldManifest.exists()) {
                continue;
            }

            try {
                WorldInfo info = WorldInfo.load(worldManifest);
                if (!info.getTitle().isEmpty()) {
                    String type = "unknown";
                    GameType gameType = CoreRegistry.get(GameTypeManager.class).getGameType(info.getGameType());
                    if( gameType!=null ) {
                        type = gameType.name();
                    }
                    String worldDescription = " (" + type + ")\n" + date.format(world.getKey());
                    UIListItem item = new UIListItem(info.getTitle() + worldDescription, info);
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
}

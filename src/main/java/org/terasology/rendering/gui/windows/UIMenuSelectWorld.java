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

import org.lwjgl.opengl.Display;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateSinglePlayer;
import org.terasology.asset.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldUtil;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIInput;
import org.terasology.rendering.gui.components.UIList;
import org.terasology.rendering.gui.dialogs.UIDialogCreateNewWorld;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.events.ClickListener;

import javax.vecmath.Vector2f;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Select world menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuSelectWorld extends UIDisplayWindow {
    private Logger logger = Logger.getLogger(getClass().getName());

    final UIImageOverlay overlay;
    final UIList list;
    final UIButton goToBack;
    final UIButton createNewWorld;
    final UIButton loadFromList;
    final UIButton deleteFromList;

    public UIMenuSelectWorld() {
        setModal(true);
        maximize();
        
        overlay = new UIImageOverlay(AssetManager.loadTexture("engine:menuBackground"));
        overlay.setVisible(true);

        list = new UIList(new Vector2f(512f, 256f));
        list.setVisible(true);

        list.addDoubleClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadSelectedWorld();
            }
        });

        goToBack = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        goToBack.getLabel().setText("Go back");
        goToBack.setVisible(true);
        goToBack.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuMain"));
            }
        });

        loadFromList = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        loadFromList.getLabel().setText("Load");
        loadFromList.setVisible(true);

        createNewWorld = new UIButton(new Vector2f(192f, 32f), UIButton.eButtonType.NORMAL);
        createNewWorld.getLabel().setText("Create new world");
        createNewWorld.setVisible(true);

        deleteFromList = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        deleteFromList.getLabel().setText("Delete");
        deleteFromList.setVisible(true);

        createNewWorld.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {

                UIDialogCreateNewWorld _window = new UIDialogCreateNewWorld("Create new world", new Vector2f(512f, 320f));
                _window.center();
                _window.clearInputControls();

                GUIManager.getInstance().addWindow(_window, "generate_world");
                GUIManager.getInstance().setFocusedWindow(_window);

                UIInput inputWorldName = (UIInput) _window.getElementById("inputWorldTitle");
                inputWorldName.setValue(_window.getWorldName());
            }
        });

        deleteFromList.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (list.getSelectedItem() == null) {
                    GUIManager.getInstance().showMessage("Error", "Please choose a world first.");
                    return;
                }

                try {
                    WorldInfo worldInfo = (WorldInfo) list.getSelectedItem().getValue();
                    File world = PathManager.getInstance().getWorldSavePath(worldInfo.getTitle());
                    WorldUtil.deleteWorld(world);
                    list.removeSelectedItem();
                } catch (Exception e) {
                    GUIManager.getInstance().showMessage("Error", "Failed deleting world data object. Sorry.");
                }
            }
        });

        loadFromList.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadSelectedWorld();
            }
        });

        fillList();

        addDisplayElement(overlay);
        addDisplayElement(list, "list");
        addDisplayElement(loadFromList, "loadFromListButton");
        addDisplayElement(goToBack, "goToBackButton");
        addDisplayElement(createNewWorld, "createWorldButton");
        addDisplayElement(deleteFromList, "deleteFromListButton");
        
        layout();
    }

    @Override
    public void layout() {
        super.layout();
        
        if (list != null) {
            list.centerHorizontally();
            list.getPosition().y = 230f;
    
            createNewWorld.getPosition().x = list.getPosition().x;
            createNewWorld.getPosition().y = list.getPosition().y + list.getSize().y + 32f;
    
            loadFromList.getPosition().x = createNewWorld.getPosition().x + createNewWorld.getSize().x + 15f;
            loadFromList.getPosition().y = createNewWorld.getPosition().y;
    
            deleteFromList.getPosition().x = loadFromList.getPosition().x + loadFromList.getSize().x + 15f;
            deleteFromList.getPosition().y = loadFromList.getPosition().y;
    
    
            goToBack.centerHorizontally();
    
            goToBack.getPosition().y = Display.getHeight() - goToBack.getSize().y - 32f;
        }
    }

    private void loadSelectedWorld() {

        if (list.size() < 1) {
            GUIManager.getInstance().showMessage("Error", "You did not create a world yet!");
            return;
        }

        if (list.getSelectedItem() == null) {
            GUIManager.getInstance().showMessage("Error", "Please choose a world!");
            return;
        }

        try {
            WorldInfo info = (WorldInfo) list.getSelectedItem().getValue();
            Config.getInstance().setDefaultSeed(info.getSeed());
            Config.getInstance().setWorldTitle(info.getTitle());
            Config.getInstance().setChunkGenerator(info.getChunkGenerators());
            CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer(info));
        } catch (Exception e) {
            GUIManager.getInstance().showMessage("Error", "Failed reading world data object. Sorry.");
        }
    }

    public void fillList() {
        list.removeAll();

        File worldCatalog = PathManager.getInstance().getWorldPath();

        for (File file : worldCatalog.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        })) {
            File worldManifest = new File(file, WorldInfo.DEFAULT_FILE_NAME);
            if (!worldManifest.exists())
                continue;
            try {
                WorldInfo info = WorldInfo.load(worldManifest);
                if (!info.getTitle().isEmpty()) {
                    list.addItem(info.getTitle(), info);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed reading world data object. Sorry.", e);
            }
        }
    }
}

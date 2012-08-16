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

    final UIImageOverlay _overlay;
    final UIList _list;
    final UIButton _goToBack;
    final UIButton _createNewWorld;
    final UIButton _loadFromList;
    final UIButton _deleteFromList;

    public UIMenuSelectWorld() {
        setModal(true);
        maximize();
        
        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:menuBackground"));
        _overlay.setVisible(true);

        _list = new UIList(new Vector2f(512f, 256f));
        _list.setVisible(true);

        _list.addDoubleClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadSelectedWorld();
            }
        });

        _goToBack = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _goToBack.getLabel().setText("Go back");
        _goToBack.setVisible(true);
        _goToBack.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuMain"));
            }
        });

        _loadFromList = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        _loadFromList.getLabel().setText("Load");
        _loadFromList.setVisible(true);

        _createNewWorld = new UIButton(new Vector2f(192f, 32f), UIButton.eButtonType.NORMAL);
        _createNewWorld.getLabel().setText("Create new world");
        _createNewWorld.setVisible(true);

        _deleteFromList = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        _deleteFromList.getLabel().setText("Delete");
        _deleteFromList.setVisible(true);

        _createNewWorld.addClickListener(new ClickListener() {
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

        _deleteFromList.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (_list.getSelectedItem() == null) {
                    GUIManager.getInstance().showMessage("Error", "Please choose a world first.");
                    return;
                }

                try {
                    WorldInfo worldInfo = (WorldInfo) _list.getSelectedItem().getValue();
                    File world = PathManager.getInstance().getWorldSavePath(worldInfo.getTitle());
                    WorldUtil.deleteWorld(world);
                    _list.removeSelectedItem();
                } catch (Exception e) {
                    GUIManager.getInstance().showMessage("Error", "Failed deleting world data object. Sorry.");
                }
            }
        });

        _loadFromList.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadSelectedWorld();
            }
        });

        fillList();

        addDisplayElement(_overlay);
        addDisplayElement(_list, "list");
        addDisplayElement(_loadFromList, "loadFromListButton");
        addDisplayElement(_goToBack, "goToBackButton");
        addDisplayElement(_createNewWorld, "createWorldButton");
        addDisplayElement(_deleteFromList, "deleteFromListButton");
        
        layout();
    }

    @Override
    public void layout() {
        super.layout();
        
        if (_list != null) {
            _list.centerHorizontally();
            _list.getPosition().y = 230f;
    
            _createNewWorld.getPosition().x = _list.getPosition().x;
            _createNewWorld.getPosition().y = _list.getPosition().y + _list.getSize().y + 32f;
    
            _loadFromList.getPosition().x = _createNewWorld.getPosition().x + _createNewWorld.getSize().x + 15f;
            _loadFromList.getPosition().y = _createNewWorld.getPosition().y;
    
            _deleteFromList.getPosition().x = _loadFromList.getPosition().x + _loadFromList.getSize().x + 15f;
            _deleteFromList.getPosition().y = _loadFromList.getPosition().y;
    
    
            _goToBack.centerHorizontally();
    
            _goToBack.getPosition().y = Display.getHeight() - _goToBack.getSize().y - 32f;
        }
    }

    private void loadSelectedWorld() {

        if (_list.size() < 1) {
            GUIManager.getInstance().showMessage("Error", "You did not create a world yet!");
            return;
        }

        if (_list.getSelectedItem() == null) {
            GUIManager.getInstance().showMessage("Error", "Please choose a world!");
            return;
        }

        try {
            WorldInfo info = (WorldInfo) _list.getSelectedItem().getValue();
            Config.getInstance().setDefaultSeed(info.getSeed());
            Config.getInstance().setWorldTitle(info.getTitle());
            Config.getInstance().setChunkGenerator(info.getChunkGenerators());
            // TODO: Need to load time too. Maybe just pass through WorldInfo?
            CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer(info));
        } catch (Exception e) {
            GUIManager.getInstance().showMessage("Error", "Failed reading world data object. Sorry.");
        }
    }

    public void fillList() {
        _list.removeAll();

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
                    _list.addItem(info.getTitle(), info);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed reading world data object. Sorry.", e);
            }
        }
    }
}

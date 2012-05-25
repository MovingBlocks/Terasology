/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.game.modes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.game.GameEngine;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.UIConfigMenu;
import org.terasology.rendering.gui.menus.UIMainMenu;
import org.terasology.rendering.gui.menus.UISelectWorldMenu;

/**
 * The class implements the main game menu.
 * <p/>
 * TODO: Add screen "Single Player"
 * TODO: Add screen "Multiplayer"
 * TODO: Add animated background
 * TODO: Add screen "Generation/Load/Delete World."
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public class StateMainMenu implements GameState {
    /* SCREENS */
    private UIMainMenu        _mainMenu;
    private UIConfigMenu      _configMenu;
    private UISelectWorldMenu _selectWorldMenu;

    private GameEngine _gameInstance = null;

    @Override
    public void init(GameEngine gameEngine) {
        _gameInstance = gameEngine;

        setupMainMenu();
        setupSelectWorldMenu();
        setupConfigMenu();

        GUIManager.getInstance().addWindow(_mainMenu, "main");
        GUIManager.getInstance().addWindow(_configMenu, "config");
        GUIManager.getInstance().addWindow(_selectWorldMenu, "selectWorld");
    }

    private void setupMainMenu() {
        _mainMenu = new UIMainMenu();
        _mainMenu.setVisible(true);

        UIButton singlePlayerButton = (UIButton)_mainMenu.getElementById("singlePlayerButton");
        UIButton configButton = (UIButton)_mainMenu.getElementById("configButton");
        UIButton exitButton   = (UIButton)_mainMenu.getElementById("exitButton");


        singlePlayerButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
              //  _mainMenu.setVisible(false);
              //  _selectWorldMenu.setVisible(true);
              _selectWorldMenu.fillList();
              GUIManager.getInstance().setFocusedWindow(_selectWorldMenu);
            }
        });

        exitButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                _gameInstance.shutdown();
            }
        });

        configButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_configMenu);
            }
        });
    }

    private void setupSelectWorldMenu(){
        _selectWorldMenu = new UISelectWorldMenu();
        _selectWorldMenu.setVisible(false);

        UIButton goToBack = (UIButton)_selectWorldMenu.getElementById("goToBackButton");

        goToBack.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_mainMenu);
            }
        });
    }

    private void setupConfigMenu() {
        _configMenu = new UIConfigMenu();
        _configMenu.setVisible(false);

        UIButton backToMainMenuButton  = (UIButton)_configMenu.getElementById("backToMainMenuButton");
        UIButton graphicsQualityButton = (UIButton)_configMenu.getElementById("graphicsQualityButton");
        UIButton FOVButton             = (UIButton)_configMenu.getElementById("fovButton");
        UIButton viewingDistanceButton = (UIButton)_configMenu.getElementById("viewingDistanceButton");

        backToMainMenuButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                _mainMenu.setVisible(true);
                _configMenu.setVisible(false);
            }
        });

        graphicsQualityButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Graphics Quality: Ugly")) {
                    Config.getInstance().setGraphicsQuality(1);
                    button.getLabel().setText("Graphics Quality: Nice");
                } else if (button.getLabel().getText().equals("Graphics Quality: Nice")) {
                    Config.getInstance().setGraphicsQuality(2);
                    button.getLabel().setText("Graphics Quality: Epic");
                } else if (button.getLabel().getText().equals("Graphics Quality: Epic")) {
                    Config.getInstance().setGraphicsQuality(0);
                    button.getLabel().setText("Graphics Quality: Ugly");
                }
            }
        });

        FOVButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                // TODO: Replace with a slider later on
                if (button.getLabel().getText().equals("Field of View: 75")) {
                    button.getLabel().setText("Field of View: 80");
                    Config.getInstance().setFov(80);
                } else if (button.getLabel().getText().equals("Field of View: 80")) {
                    button.getLabel().setText("Field of View: 85");
                    Config.getInstance().setFov(85);
                } else if (button.getLabel().getText().equals("Field of View: 85")) {
                    button.getLabel().setText("Field of View: 90");
                    Config.getInstance().setFov(90);
                } else if (button.getLabel().getText().equals("Field of View: 90")) {
                    button.getLabel().setText("Field of View: 95");
                    Config.getInstance().setFov(95);
                } else if (button.getLabel().getText().equals("Field of View: 95")) {
                    button.getLabel().setText("Field of View: 100");
                    Config.getInstance().setFov(100);
                } else if (button.getLabel().getText().equals("Field of View: 100")) {
                    button.getLabel().setText("Field of View: 105");
                    Config.getInstance().setFov(105);
                } else if (button.getLabel().getText().equals("Field of View: 105")) {
                    button.getLabel().setText("Field of View: 110");
                    Config.getInstance().setFov(110);
                } else if (button.getLabel().getText().equals("Field of View: 110")) {
                    button.getLabel().setText("Field of View: 115");
                    Config.getInstance().setFov(115);
                } else if (button.getLabel().getText().equals("Field of View: 115")) {
                    button.getLabel().setText("Field of View: 120");
                    Config.getInstance().setFov(120);
                } else if (button.getLabel().getText().equals("Field of View: 120")) {
                    button.getLabel().setText("Field of View: 75");
                    Config.getInstance().setFov(75);
                }
            }
        });

        viewingDistanceButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Viewing Distance: Near")) {
                    button.getLabel().setText("Viewing Distance: Moderate");
                    Config.getInstance().setViewingDistanceById(1);
                } else if (button.getLabel().getText().equals("Viewing Distance: Moderate")) {
                    button.getLabel().setText("Viewing Distance: Far");
                    Config.getInstance().setViewingDistanceById(2);
                } else if (button.getLabel().getText().equals("Viewing Distance: Far")) {
                    Config.getInstance().setViewingDistanceById(3);
                    button.getLabel().setText("Viewing Distance: Ultra");
                } else if (button.getLabel().getText().equals("Viewing Distance: Ultra")) {
                    Config.getInstance().setViewingDistanceById(0);
                    button.getLabel().setText("Viewing Distance: Near");
                }
            }
        });
    }

    @Override
    public void activate() {
        Mouse.setGrabbed(false);
        playBackgroundMusic();

        GUIManager.getInstance().getWindowById("main").setVisible(true);
        GUIManager.getInstance().getWindowById("config").setVisible(false);
        GUIManager.getInstance().getWindowById("selectWorld").setVisible(false);
        GUIManager.getInstance().getWindowById("generate_world").setVisible(false);
        GUIManager.getInstance().setFocusedWindow("main");

        UIButton graphicsQualityButton = (UIButton)_configMenu.getElementById("graphicsQualityButton");
        UIButton FOVButton             = (UIButton)_configMenu.getElementById("fovButton");
        UIButton viewingDistanceButton = (UIButton)_configMenu.getElementById("viewingDistanceButton");

        if (Config.getInstance().getActiveViewingDistanceId() == 3)
            viewingDistanceButton.getLabel().setText("Viewing Distance: Ultra");
        else if (Config.getInstance().getActiveViewingDistanceId() == 1)
            viewingDistanceButton.getLabel().setText("Viewing Distance: Moderate");
        else if (Config.getInstance().getActiveViewingDistanceId() == 2)
            viewingDistanceButton.getLabel().setText("Viewing Distance: Far");
        else
            viewingDistanceButton.getLabel().setText("Viewing Distance: Near");

        if (Config.getInstance().getGraphicsQuality() == 1)
            graphicsQualityButton.getLabel().setText("Graphics Quality: Nice");
        else if (Config.getInstance().getGraphicsQuality() == 2)
            graphicsQualityButton.getLabel().setText("Graphics Quality: Epic");
        else
            graphicsQualityButton.getLabel().setText("Graphics Quality: Ugly");

        // TODO: Replace with a slider later on
        FOVButton.getLabel().setText("Field of View: " + (int) Config.getInstance().getFov());
    }

    @Override
    public void deactivate() {
        stopBackgroundMusic();
        GUIManager.getInstance().closeWindows();
    }

    @Override
    public void dispose() {
        // Nothing to do here.
    }

    private void playBackgroundMusic() {
        AudioManager.playMusic("engine:resurface");
    }

    private void stopBackgroundMusic() {
        AudioManager.getInstance().stopAllSounds();
    }

    @Override
    public void handleInput(float delta) {
        processKeyboardInput();
        processMouseInput();
    }

    @Override
    public void update(float delta) {
        updateUserInterface();
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        renderUserInterface();
    }

    public void renderUserInterface() {
        GUIManager.getInstance().render();
    }

    private void updateUserInterface() {
        GUIManager.getInstance().update();
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    private void processKeyboardInput() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                int key = Keyboard.getEventKey();

                if (!Keyboard.isRepeatEvent()) {
                    if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                        _gameInstance.shutdown();
                        return;
                    }
                }
                GUIManager.getInstance().processKeyboardInput(key);
            }
        }
    }

    /*
    * Process mouse input - nothing system-y, so just passing it to the Player class
    */
    private void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();

            GUIManager.getInstance().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
        }
    }
}

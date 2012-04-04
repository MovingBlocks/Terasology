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

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.UIConfigMenu;
import org.terasology.rendering.gui.menus.UIMainMenu;
import org.terasology.rendering.gui.menus.UISelectWorldMenu;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

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
public class StateMainMenu implements IGameState {

    /* GUI */
   // private ArrayList<UIDisplayElement> _guiScreens = new ArrayList<UIDisplayElement>();

    /* SCREENS */
    private UIMainMenu        _mainMenu;
    private UIConfigMenu      _configMenu;
    private UISelectWorldMenu _selectWorldMenu;

    private Terasology _gameInstance = null;

    public void init() {
        _gameInstance = Terasology.getInstance();

        setupMainMenu();
        setupConfigMenu();

        GUIManager.getInstance().addWindow(_mainMenu);
        GUIManager.getInstance().addWindow(_configMenu);
        GUIManager.getInstance().addWindow(_selectWorldMenu);
    }

    private void setupMainMenu() {
        _mainMenu = new UIMainMenu();
        _mainMenu.setVisible(true);

        _selectWorldMenu = new UISelectWorldMenu();
        _selectWorldMenu.setVisible(false);

        _mainMenu.getSinglePlayerButton().addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                _mainMenu.setVisible(false);
                _selectWorldMenu.setVisible(true);
            }
        });

        _mainMenu.getExitButton().addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                Terasology.getInstance().exit();
            }
        });

        _mainMenu.getConfigButton().addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                _mainMenu.setVisible(false);
                _configMenu.setVisible(true);
            }
        });

        _selectWorldMenu.getGoToBackButton().addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                _selectWorldMenu.setVisible(false);
                _mainMenu.setVisible(true);
            }
        });
    }

    private void setupConfigMenu() {
        _configMenu = new UIConfigMenu();

        _configMenu.getBackToMainMenuButton().addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                _mainMenu.setVisible(true);
                _configMenu.setVisible(false);
            }
        });

        _configMenu.getGraphicsQualityButton().addClickListener(new IClickListener() {
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

        _configMenu.getFOVButton().addClickListener(new IClickListener() {
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
                    button.getLabel().setText("Field of View: 75");
                    Config.getInstance().setFov(75);
                }
            }
        });

        _configMenu.getViewingDistanceButton().addClickListener(new IClickListener() {
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

    public void activate() {
        Mouse.setGrabbed(false);
        playBackgroundMusic();

        if (Config.getInstance().getActiveViewingDistanceId() == 3)
            _configMenu.getViewingDistanceButton().getLabel().setText("Viewing Distance: Ultra");
        else if (Config.getInstance().getActiveViewingDistanceId() == 1)
            _configMenu.getViewingDistanceButton().getLabel().setText("Viewing Distance: Moderate");
        else if (Config.getInstance().getActiveViewingDistanceId() == 2)
            _configMenu.getViewingDistanceButton().getLabel().setText("Viewing Distance: Far");
        else
            _configMenu.getViewingDistanceButton().getLabel().setText("Viewing Distance: Near");

        if (Config.getInstance().getGraphicsQuality() == 1)
            _configMenu.getGraphicsQualityButton().getLabel().setText("Graphics Quality: Nice");
        else if (Config.getInstance().getGraphicsQuality() == 2)
            _configMenu.getGraphicsQualityButton().getLabel().setText("Graphics Quality: Epic");
        else
            _configMenu.getGraphicsQualityButton().getLabel().setText("Graphics Quality: Ugly");

        // TODO: Replace with a slider later on
        _configMenu.getFOVButton().getLabel().setText("Field of View: " + (int) Config.getInstance().getFov());
    }

    public void deactivate() {
        stopBackgroundMusic();
    }

    public void dispose() {
        // Nothing to do here.
    }

    private void playBackgroundMusic() {
        AudioManager.playMusic("Resurface");
    }

    private void stopBackgroundMusic() {
        AudioManager.getInstance().stopAllSounds();
    }

    public void update(double delta) {
        updateUserInterface();
    }

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
    public void processKeyboardInput() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    _gameInstance.exit();
                    return;
                }
            }
            GUIManager.getInstance().processKeyboardInput(key);
        }
    }

    /*
    * Process mouse input - nothing system-y, so just passing it to the Player class
    */
    public void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();

            GUIManager.getInstance().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
        }
    }
}

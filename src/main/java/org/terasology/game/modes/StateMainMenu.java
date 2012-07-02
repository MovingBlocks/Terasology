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
package org.terasology.game.modes;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.game.GameEngine;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.*;

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
public class StateMainMenu implements GameState {
    /* SCREENS */
    private UIMainMenu _mainMenu;
    private UIConfigMenu _configMenu;
    private UIVideoOptions _videoOptionsMenu;
    private UISoundOptions _soundOptionsMenu;
    private UIInputOptions _inputOptionsMenu;
    private UIModOptions _modOptionMenu;
    private UISelectWorldMenu _selectWorldMenu;

    private GameEngine _gameInstance = null;

    @Override
    public void init(GameEngine gameEngine) {
        _gameInstance = gameEngine;

        setupMainMenu();
        setupSelectWorldMenu();
        setupConfigMenu();
        setupVideoOptionsMenu();
        setupSoundOptionsMenu();
        setupInputOptionsMenu();
        setupModOptionsMenu();

        GUIManager.getInstance().addWindow(_mainMenu, "main");
        GUIManager.getInstance().addWindow(_configMenu, "config");
        GUIManager.getInstance().addWindow(_selectWorldMenu, "selectWorld");
        GUIManager.getInstance().addWindow(_videoOptionsMenu, "videoOptions");
        GUIManager.getInstance().addWindow(_soundOptionsMenu, "soundOptions");
        GUIManager.getInstance().addWindow(_inputOptionsMenu, "inputOptions");
        GUIManager.getInstance().addWindow(_modOptionMenu, "modOptions");
    }

    private void setupMainMenu() {
        _mainMenu = new UIMainMenu();
        _mainMenu.setVisible(true);

        UIButton singlePlayerButton = (UIButton) _mainMenu.getElementById("singlePlayerButton");
        UIButton configButton = (UIButton) _mainMenu.getElementById("configButton");
        UIButton exitButton = (UIButton) _mainMenu.getElementById("exitButton");


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

    private void setupSelectWorldMenu() {
        _selectWorldMenu = new UISelectWorldMenu();
        _selectWorldMenu.setVisible(false);

        UIButton goToBack = (UIButton) _selectWorldMenu.getElementById("goToBackButton");

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

        UIButton backToMainMenuButton = (UIButton) _configMenu.getElementById("backToMainMenuButton");
        UIButton videoOptionsButton = (UIButton) _configMenu.getElementById("videoOptionsButton");
        UIButton soundOptionsButton = (UIButton) _configMenu.getElementById("soundOptionsButton");
        UIButton inputOptionsButton = (UIButton) _configMenu.getElementById("inputOptionsButton");
        UIButton modOptionsButton = (UIButton) _configMenu.getElementById("modOptionsButton");

        backToMainMenuButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_mainMenu);
            }
        });

        videoOptionsButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_videoOptionsMenu);
            }
        });

        soundOptionsButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_soundOptionsMenu);
            }
        });

        inputOptionsButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_inputOptionsMenu);
            }
        });

        modOptionsButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_modOptionMenu);
            }
        });


    }

    private void setupVideoOptionsMenu() {
        _videoOptionsMenu = new UIVideoOptions();
        _videoOptionsMenu.setVisible(false);

        UIButton videoToSettingsMenuButton = (UIButton) _videoOptionsMenu.getElementById("backToSettingsMenuButton");
        UIButton graphicsQualityButton = (UIButton) _videoOptionsMenu.getElementById("graphicsQualityButton");
        UIButton FOVButton = (UIButton) _videoOptionsMenu.getElementById("fovButton");
        UIButton viewingDistanceButton = (UIButton) _videoOptionsMenu.getElementById("viewingDistanceButton");
        UIButton animateGrassButton = (UIButton) _videoOptionsMenu.getElementById("animateGrassButton");
        UIButton reflectiveWaterButton = (UIButton) _videoOptionsMenu.getElementById("reflectiveWaterButton");
        UIButton blurIntensityButton = (UIButton) _videoOptionsMenu.getElementById("blurIntensityButton");
        UIButton bobbingButton = (UIButton) _videoOptionsMenu.getElementById("bobbingButton");

        videoToSettingsMenuButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_configMenu);
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

        animateGrassButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Animated grass: false")) {
                    button.getLabel().setText("Animated grass: true");
                    Config.getInstance().setAnimatedGrass(true);
                } else if (button.getLabel().getText().equals("Animated grass: true")) {
                    button.getLabel().setText("Animated grass: false");
                    Config.getInstance().setAnimatedGrass(false);
                }
            }
        });

        reflectiveWaterButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Reflective water: false")) {
                    button.getLabel().setText("Reflective water: true");
                    Config.getInstance().setComplexWater(true);
                } else if (button.getLabel().getText().equals("Reflective water: true")) {
                    button.getLabel().setText("Reflective water: false");
                    Config.getInstance().setComplexWater(false);
                }
            }
        });

        blurIntensityButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Blur intensity: Normal")) {
                    button.getLabel().setText("Blur intensity: Max");
                    Config.getInstance().setBlurIntensity(3);
                } else if (button.getLabel().getText().equals("Blur intensity: Max")) {
                    button.getLabel().setText("Blur intensity: Off");
                    Config.getInstance().setBlurIntensity(0);
                } else if (button.getLabel().getText().equals("Blur intensity: Off")) {
                    button.getLabel().setText("Blur intensity: Some");
                    Config.getInstance().setBlurIntensity(1);
                } else if (button.getLabel().getText().equals("Blur intensity: Some")) {
                    button.getLabel().setText("Blur intensity: Normal");
                    Config.getInstance().setBlurIntensity(2);
                }
            }
        });

        bobbingButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Bobbing: true")) {
                    button.getLabel().setText("Bobbing: false");
                    Config.getInstance().setCameraBobbing(false);
                } else if (button.getLabel().getText().equals("Bobbing: false")) {
                    button.getLabel().setText("Bobbing: true");
                    Config.getInstance().setCameraBobbing(true);
                }
            }
        });
    }

    private void setupSoundOptionsMenu() {
        _soundOptionsMenu = new UISoundOptions();
        _soundOptionsMenu.setVisible(false);

        UIButton soundToSettingsMenuButton = (UIButton) _soundOptionsMenu.getElementById("soundToSettingsMenuButton");
        soundToSettingsMenuButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_configMenu);
            }
        });
    }

    private void setupInputOptionsMenu() {
        _inputOptionsMenu = new UIInputOptions();
        _inputOptionsMenu.setVisible(false);

        UIButton inputToSettingsMenuButton = (UIButton) _inputOptionsMenu.getElementById("inputToSettingsMenuButton");
        inputToSettingsMenuButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_configMenu);
            }
        });
    }

    private void setupModOptionsMenu() {
        _modOptionMenu = new UIModOptions();
        _modOptionMenu.setVisible(false);

        UIButton backToSettingsMenuButton = (UIButton) _modOptionMenu.getElementById("backToSettingsMenuButton");
        UIButton minionsButton = (UIButton) _modOptionMenu.getElementById("minionsButton");
        UIButton minionOptionsButton = (UIButton) _modOptionMenu.getElementById("minionOptionsButton");

        backToSettingsMenuButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                GUIManager.getInstance().setFocusedWindow(_configMenu);
            }
        });

        minionOptionsButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                _modOptionMenu.setVisible(true);
                _configMenu.setVisible(false);
            }
        });

        minionsButton.addClickListener(new IClickListener() {
            @Override
            public void clicked(UIDisplayElement element) {
                UIButton button = (UIButton) element;

                if (button.getLabel().getText().equals("Minions enabled : false")) {
                    Config.getInstance().setGraphicsQuality(1);
                    button.getLabel().setText("Minions enabled : true");
                } else if (button.getLabel().getText().equals("Minions enabled : true")) {
                    Config.getInstance().setGraphicsQuality(1);
                    button.getLabel().setText("Minions enabled : false");
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
        GUIManager.getInstance().getWindowById("videoOptions").setVisible(false);
        GUIManager.getInstance().getWindowById("selectWorld").setVisible(false);
        GUIManager.getInstance().getWindowById("generate_world").setVisible(false);
        GUIManager.getInstance().setFocusedWindow("main");

        UIButton graphicsQualityButton = (UIButton) _videoOptionsMenu.getElementById("graphicsQualityButton");
        UIButton FOVButton = (UIButton) _videoOptionsMenu.getElementById("fovButton");
        UIButton viewingDistanceButton = (UIButton) _videoOptionsMenu.getElementById("viewingDistanceButton");
        UIButton animateGrassButton = (UIButton) _videoOptionsMenu.getElementById("animateGrassButton");
        UIButton reflectiveWaterButton = (UIButton) _videoOptionsMenu.getElementById("reflectiveWaterButton");
        UIButton blurIntensityButton = (UIButton) _videoOptionsMenu.getElementById("blurIntensityButton");
        UIButton bobbingButton = (UIButton) _videoOptionsMenu.getElementById("bobbingButton");

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

        if (Config.getInstance().isAnimatedGrass()) {
            animateGrassButton.getLabel().setText("Animated grass: true");
        } else {
            animateGrassButton.getLabel().setText("Animated grass: false");
        }

        if (Config.getInstance().isComplexWater()) {
            reflectiveWaterButton.getLabel().setText("Reflective water: true");
        } else {
            reflectiveWaterButton.getLabel().setText("Reflective water: false");
        }

        if (Config.getInstance().getBlurIntensity() == 0) {
            blurIntensityButton.getLabel().setText("Blur intensity: Off");
        } else if (Config.getInstance().getBlurIntensity() == 1) {
            blurIntensityButton.getLabel().setText("Blur intensity: Some");
        } else if (Config.getInstance().getBlurIntensity() == 2) {
            blurIntensityButton.getLabel().setText("Blur intensity: Normal");
        } else if (Config.getInstance().getBlurIntensity() == 3) {
            blurIntensityButton.getLabel().setText("Blur intensity: Max");
        }

        if (Config.getInstance().isCameraBobbing()) {
            bobbingButton.getLabel().setText("Bobbing: true");
        } else {
            bobbingButton.getLabel().setText("Bobbing: false");
        }
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

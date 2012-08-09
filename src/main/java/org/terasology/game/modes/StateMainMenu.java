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
import org.terasology.rendering.gui.components.UISlider;
import org.terasology.rendering.gui.components.UIStateButton;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
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
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @version 0.2
 */
public class StateMainMenu implements GameState {
    /* SCREENS */
    private UIMainMenu _mainMenu;
    private UIConfigMenu _configMenu;
    private UIConfigMenuVideo _configMenuVideo;
    private UIConfigMenuAudio _configMenuAudio;
    private UIConfigMenuControls _configMenuControls;
    private UIConfigMenuMods _configMenuMods;
    private UISelectWorldMenu _selectWorldMenu;

    private GameEngine _gameInstance = null;

    @Override
    public void init(GameEngine gameEngine) {
        _gameInstance = gameEngine;

        setupMainMenu();
        setupSelectWorldMenu();
        setupConfigMenu();
        setupConfigMenuVideo();
        setupConfigMenuAudio();
        setupConfigMenuControls();
        setupConfigMenuMods();

        GUIManager.getInstance().addWindow(_mainMenu, "main");
        GUIManager.getInstance().addWindow(_configMenu, "config");
        GUIManager.getInstance().addWindow(_selectWorldMenu, "selectWorld");
        GUIManager.getInstance().addWindow(_configMenuVideo, "videoOptions");
        GUIManager.getInstance().addWindow(_configMenuAudio, "soundOptions");
        GUIManager.getInstance().addWindow(_configMenuControls, "inputOptions");
        GUIManager.getInstance().addWindow(_configMenuMods, "modOptions");
    }

    private void setupMainMenu() {
        _mainMenu = new UIMainMenu();
        _mainMenu.setVisible(true);

        UIButton singlePlayerButton = (UIButton) _mainMenu.getElementById("singlePlayerButton");
        UIButton configButton = (UIButton) _mainMenu.getElementById("configButton");
        UIButton exitButton = (UIButton) _mainMenu.getElementById("exitButton");


        singlePlayerButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
            	_selectWorldMenu.fillList();
            	GUIManager.getInstance().setFocusedWindow(_selectWorldMenu);
			}
        });

        exitButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				_gameInstance.shutdown();
			}
        });

        configButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenu);
			}
        });
    }

    private void setupSelectWorldMenu() {
        _selectWorldMenu = new UISelectWorldMenu();
        _selectWorldMenu.setVisible(false);

        UIButton goToBack = (UIButton) _selectWorldMenu.getElementById("goToBackButton");

        goToBack.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_mainMenu);
			}
        });
    }

    private void setupConfigMenu() {
        _configMenu = new UIConfigMenu();
        _configMenu.setVisible(false);

        UIButton videoButton = (UIButton) _configMenu.getElementById("videoButton");
        UIButton audioButton = (UIButton) _configMenu.getElementById("audioButton");
        UIButton controlsButton = (UIButton) _configMenu.getElementById("controlsButton");
        UIButton modsButton = (UIButton) _configMenu.getElementById("modsButton");
        UIButton backToMainMenuButton = (UIButton) _configMenu.getElementById("backToMainMenuButton");

        videoButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenuVideo);
			}
        });

        audioButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenuAudio);
			}
        });

        controlsButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenuControls);
			}
        });

        modsButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenuMods);
			}
        });

        backToMainMenuButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_mainMenu);
			}
        });
    }

    private void setupConfigMenuVideo() {
        _configMenuVideo = new UIConfigMenuVideo();
        _configMenuVideo.setVisible(false);

        UIButton videoToSettingsMenuButton = (UIButton) _configMenuVideo.getElementById("backToConfigMenuButton");
        videoToSettingsMenuButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenu);
			}
        });
    }

    private void setupConfigMenuAudio() {
    	_configMenuAudio = new UIConfigMenuAudio();
        _configMenuAudio.setVisible(false);

        UIButton backToConfigMenuButton = (UIButton) _configMenuAudio.getElementById("backToConfigMenuButton");
        backToConfigMenuButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenu);
			}
        });
    }

    private void setupConfigMenuControls() {
    	_configMenuControls = new UIConfigMenuControls();
        _configMenuControls.setVisible(false);

        UIButton backToSettingsMenuButton = (UIButton) _configMenuControls.getElementById("backToConfigMenuButton");

        backToSettingsMenuButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenu);
			}
        });
    }

    private void setupConfigMenuMods() {
    	_configMenuMods = new UIConfigMenuMods();
        _configMenuMods.setVisible(false);

        UIButton minionsButton = (UIButton) _configMenuMods.getElementById("minionsButton");
        UIButton minionOptionsButton = (UIButton) _configMenuMods.getElementById("minionOptionsButton");
        UIButton backToSettingsMenuButton = (UIButton) _configMenuMods.getElementById("backToConfigMenuButton");

        minionOptionsButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
	            _configMenuMods.setVisible(true);
	            _configMenu.setVisible(false);
			}
        });

        minionsButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
                UIButton b = (UIButton) element;

                if (b.getLabel().getText().equals("Minions enabled : false")) {
                    
                    b.getLabel().setText("Minions enabled : true");
                } else if (b.getLabel().getText().equals("Minions enabled : true")) {
                    
                    b.getLabel().setText("Minions enabled : false");
                }
			}
        });
        
        backToSettingsMenuButton.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				GUIManager.getInstance().setFocusedWindow(_configMenu);
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

        UIStateButton graphicsQualityButton = (UIStateButton) _configMenuVideo.getElementById("graphicsQualityButton");
        UISlider fovSlider = (UISlider) _configMenuVideo.getElementById("fovSlider");
        UIStateButton viewingDistanceButton = (UIStateButton) _configMenuVideo.getElementById("viewingDistanceButton");
        UIStateButton animateGrassButton = (UIStateButton) _configMenuVideo.getElementById("animateGrassButton");
        UIStateButton reflectiveWaterButton = (UIStateButton) _configMenuVideo.getElementById("reflectiveWaterButton");
        UIStateButton blurIntensityButton = (UIStateButton) _configMenuVideo.getElementById("blurIntensityButton");
        UIStateButton bobbingButton = (UIStateButton) _configMenuVideo.getElementById("bobbingButton");
        
        UISlider soundSlider = (UISlider) _configMenuAudio.getElementById("soundVolumeSlider");
        UISlider musicSlider = (UISlider) _configMenuAudio.getElementById("musicVolumeSlider");
        
        fovSlider.setValue((int)Config.getInstance().getFov());
        viewingDistanceButton.setState(Config.getInstance().getActiveViewingDistanceId());
        blurIntensityButton.setState(Config.getInstance().getBlurIntensity());
        soundSlider.setValue(Config.getInstance().getSoundVolume());
        musicSlider.setValue(Config.getInstance().getMusicVolume());
        
        if (Config.getInstance().isEnablePostProcessingEffects() && Config.getInstance().isFlickeringLight())
            graphicsQualityButton.setState(2);
        else if (!Config.getInstance().isEnablePostProcessingEffects() && Config.getInstance().isFlickeringLight())
        	graphicsQualityButton.setState(1);
        else
        	graphicsQualityButton.setState(0);
        
        if (Config.getInstance().isAnimatedGrass()) {
            animateGrassButton.setState(1);
        } else {
        	animateGrassButton.setState(0);
        }

        if (Config.getInstance().isComplexWater()) {
            reflectiveWaterButton.setState(1);
        } else {
            reflectiveWaterButton.setState(0);
        }

        if (Config.getInstance().isCameraBobbing()) {
            bobbingButton.setState(1);
        } else {
            bobbingButton.setState(0);
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

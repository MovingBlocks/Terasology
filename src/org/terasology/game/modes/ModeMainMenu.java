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
import org.lwjgl.opengl.Display;
import org.newdawn.slick.openal.SoundStore;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.UIMainMenu;
import org.terasology.rendering.world.WorldRenderer;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

//OpenGL
//GUI

/**
 * The class implements the main game menu. The class contains the following screens: game settings,
 * single player, multiplayer games, generation / load / delete the world.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 * @todo Create a parent class GameMode.
 * @todo Add screen "Single Player"
 * @todo Add animated background
 * @todo Add screen "Game settings"
 * @todo Add screen "Multiplayer Player"
 * @todo Add screen "generation / load / delete the world."
 */
public class ModeMainMenu implements IGameMode {

    //GUI
    private ArrayList<UIDisplayElement> _guiScreens = new ArrayList<UIDisplayElement>();

    /*SCREENS*/
    private UIMainMenu _mainMenu;

    private Terasology _gameInstance = null;

    public void init() {
        _gameInstance = Terasology.getInstance();
        _mainMenu = new UIMainMenu();
        _mainMenu.setVisible(true);
        _guiScreens.add(_mainMenu);
        Mouse.setGrabbed(false);
        Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
    }

    public void activate() {
        playBackgroundMusic();
    }

    public void deactivate() {
        stopBackgroundMusic();
    }

    private void playBackgroundMusic() {
        SoundStore.get().setMusicVolume(0.1f);
        AudioManager.getInstance().getAudio("Resurface").playAsMusic(1.0f, 1.0f, true);
    }

    private void stopBackgroundMusic() {
        AudioManager.getInstance().stopAllSounds();
    }

    public void update(double delta) {
        updateUserInterface();
    }

    /*
    * In the future, to make in the parent class GameMode
    */
    private boolean screenHasFocus() {
        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay()) {
                return true;
            }
        }

        return false;
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        renderUserInterface();
    }

    public void renderUserInterface() {
        for (UIDisplayElement screen : _guiScreens) {
            screen.render();
        }
    }
    private void updateUserInterface() {
        for (UIDisplayElement screen : _guiScreens) {
            screen.update();
        }
    }

    public WorldRenderer getActiveWorldRenderer() {
        return null;
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    public void processKeyboardInput() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    _gameInstance.exit(false);
                    return;
                }
                // Pass input to focused GUI element
                for (UIDisplayElement screen : _guiScreens) {
                    if (screenCanFocus(screen)) {
                        screen.processKeyboardInput(key);
                    }
                }
            }
        }
    }

    /*
    * Process mouse input - nothing system-y, so just passing it to the Player class
    */
    public void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();

            for (UIDisplayElement screen : _guiScreens) {
                if (screenCanFocus(screen)) {
                    screen.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
                }
            }
        }
    }

    private boolean screenCanFocus(UIDisplayElement s) {
        boolean result = true;

        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay() && screen != s)
                result = false;
        }

        return result;
    }

    /*
    * This is a temporary cap. In the future, will be removed
    */
    public void updatePlayerInput() {
        return;
    }

}

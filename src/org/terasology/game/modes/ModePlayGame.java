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
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.Config;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.*;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import java.util.ArrayList;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

//OpenGL
//GUI

/**
 * Play mode
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public class ModePlayGame implements IGameMode {

    /* GUI */
    private ArrayList<UIDisplayElement> _guiScreens = new ArrayList<UIDisplayElement>();
    private UIHeadsUpDisplay _hud;
    private UIMetrics _metrics;
    private UIPauseMenu _pauseMenu;
    private UILoadingScreen _loadingScreen;
    private UIStatusScreen _statusScreen;
    private UIInventoryScreen _inventoryScreen;

    /* CONST */
    private static final int TICKS_PER_SECOND = 60;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;

    /* RENDERING */
    private WorldRenderer _activeWorldRenderer;

    public double _timeAccumulator = 0;

    /* VIEWING DISTANCE */
    private static final int[] VIEWING_DISTANCES = {
            Config.getInstance().getViewingDistanceNear(),
            Config.getInstance().getViewingDistanceModerate(),
            Config.getInstance().getViewingDistanceFar(),
            Config.getInstance().getViewingDistanceUltra()
    };

    private int _activeViewingDistance = 0;

    /* GAME LOOP */
    private boolean _pauseGame = false;

    private Terasology _gameInstance = null;

    public void init() {
        _gameInstance = Terasology.getInstance();

        _hud = new UIHeadsUpDisplay();
        _hud.setVisible(true);

        _pauseMenu = new UIPauseMenu();
        _loadingScreen = new UILoadingScreen();
        _statusScreen = new UIStatusScreen();
        _inventoryScreen = new UIInventoryScreen();
        _metrics = new UIMetrics();

        _metrics.setVisible(true);

        _guiScreens.add(_metrics);
        _guiScreens.add(_hud);
        _guiScreens.add(_pauseMenu);
        _guiScreens.add(_loadingScreen);
        _guiScreens.add(_inventoryScreen);
        _guiScreens.add(_statusScreen);

        resetOpenGLParameters();
    }

    public void activate() {
        String worldSeed = Config.getInstance().getDefaultSeed();

        if (worldSeed.isEmpty()) {
            worldSeed = null;
        }

        initWorld("World1", worldSeed);
    }

    public void deactivate() {
        _activeWorldRenderer.dispose();
        _activeWorldRenderer = null;
    }

    public void update() {
        //while (_timeAccumulator >= SKIP_TICKS) {
            if (_activeWorldRenderer != null && shouldUpdateWorld())
                _activeWorldRenderer.update();

            if (screenHasFocus() || !shouldUpdateWorld()) {
                if (Mouse.isGrabbed()) {
                    Mouse.setGrabbed(false);
                    Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
                }

            } else {
                if (!Mouse.isGrabbed())
                    Mouse.setGrabbed(true);
            }

            if (_activeWorldRenderer != null) {
                if (_activeWorldRenderer.getPlayer().isDead()) {
                    _statusScreen.setVisible(true);
                    _statusScreen.updateStatus("Sorry. You've died. :-(");
                } else {
                    _statusScreen.setVisible(false);
                }

            }

            //_timeAccumulator -= SKIP_TICKS;
        //}
    }

    public void updateTimeAccumulator(double delta) {
        _timeAccumulator += delta;
    }

    public void initWorld(String title) {
        initWorld(title, null);
    }

    /**
     * Init. a new random world.
     */
    public void initWorld(String title, String seed) {
        final FastRandom random = new FastRandom();

        // Get rid of the old world
        if (_activeWorldRenderer != null) {
            _activeWorldRenderer.dispose();
            _activeWorldRenderer = null;
        }

        if (seed == null) {
            seed = random.randomCharacterString(16);
        } else if (seed.isEmpty()) {
            seed = random.randomCharacterString(16);
        }

        Terasology.getInstance().getLogger().log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Init. a new world
        _activeWorldRenderer = new WorldRenderer(title, seed);
        _activeWorldRenderer.setPlayer(new Player(_activeWorldRenderer));

        // Create the first Portal if it doesn't exist yet
        _activeWorldRenderer.initPortal();
        _activeWorldRenderer.setViewingDistance(VIEWING_DISTANCES[_activeViewingDistance]);

        simulateWorld(4000);
    }

    private boolean screenHasFocus() {
        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay()) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldUpdateWorld() {
        return !_pauseGame && !_pauseMenu.isVisible();
    }

    public void resetOpenGLParameters() {
        // Update the viewing distance
        double minDist = (VIEWING_DISTANCES[_activeViewingDistance] / 2) * 16.0f;
        glFogf(GL_FOG_START, (float) (minDist * 0.001));
        glFogf(GL_FOG_END, (float) minDist);
    }

    private void simulateWorld(int duration) {
        long timeBefore = _gameInstance.getTimeInMs();

        _loadingScreen.setVisible(true);
        _hud.setVisible(false);
        _metrics.setVisible(false);

        float diff = 0;

        while (diff < duration) {
            _loadingScreen.updateStatus(String.format("Fast forwarding world... %.2f%%! :-)", (diff / duration) * 100f));

            renderUserInterface();
            updateUserInterface();

            getActiveWorldRenderer().standaloneGenerateChunks();

            Display.update();

            diff = _gameInstance.getTimeInMs() - timeBefore;
        }

        _loadingScreen.setVisible(false);
        _hud.setVisible(true);
        _metrics.setVisible(true);
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (_activeWorldRenderer != null) {
            _activeWorldRenderer.render();
        }

        PerformanceMonitor.startActivity("RenderAndUpdate UI");
        renderUserInterface();
        updateUserInterface();
        PerformanceMonitor.endActivity();
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
        return _activeWorldRenderer;
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    public void processKeyboardInput() {
        boolean debugEnabled = Config.getInstance().isDebug();

        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                if (key == Keyboard.KEY_ESCAPE) {
                    togglePauseMenu();
                }

                if (key == Keyboard.KEY_I) {
                    toggleInventory();
                }

                if (key == Keyboard.KEY_F3) {
                    Config.getInstance().setDebug(!Config.getInstance().isDebug());
                }

                if (key == Keyboard.KEY_F) {
                    toggleViewingDistance();
                }

                if (key == Keyboard.KEY_F12) {
                    Terasology.getInstance().getActiveWorldRenderer().printScreen();
                }

                // Pass input to focused GUI element
                for (UIDisplayElement screen : _guiScreens) {
                    if (screenCanFocus(screen)) {
                        screen.processKeyboardInput(key);
                    }
                }
            }

            // Features for debug mode only
            if (debugEnabled) {
                if (key == Keyboard.KEY_UP && Keyboard.getEventKeyState()) {
                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() + 0.005);
                }

                if (key == Keyboard.KEY_DOWN && Keyboard.getEventKeyState()) {
                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() - 0.005);
                }

                if (key == Keyboard.KEY_RIGHT && Keyboard.getEventKeyState()) {
                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() + 0.02);
                }

                if (key == Keyboard.KEY_LEFT && Keyboard.getEventKeyState()) {
                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() - 0.02);
                }
                if (key == Keyboard.KEY_R && Keyboard.getEventKeyState()) {
                    getActiveWorldRenderer().setWireframe(!getActiveWorldRenderer().isWireframe());
                }
            }

            // Pass input to the current player
            if (!screenHasFocus())
                _activeWorldRenderer.getPlayer().processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
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

            if (!screenHasFocus())
                _activeWorldRenderer.getPlayer().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
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

    public void updatePlayerInput() {
        if (!screenHasFocus())
            getActiveWorldRenderer().getPlayer().updateInput();
    }

    public void pause() {
        _pauseGame = true;
    }

    public void unpause() {
        _pauseGame = false;
    }

    public void togglePauseGame() {
        if (_pauseGame) {
            unpause();
        } else {
            pause();
        }
    }

    private void toggleInventory() {
        if (screenCanFocus(_inventoryScreen))
            _inventoryScreen.setVisible(!_inventoryScreen.isVisible());
    }

    public void togglePauseMenu() {
        if (screenCanFocus(_pauseMenu)) {
            _pauseMenu.setVisible(!_pauseMenu.isVisible());
        }
    }

    public void toggleViewingDistance() {
        _activeViewingDistance = (_activeViewingDistance + 1) % 4;
        _activeWorldRenderer.setViewingDistance(VIEWING_DISTANCES[_activeViewingDistance]);
    }


    public boolean isGamePaused() {
        return _pauseGame;
    }

    public IWorldProvider getActiveWorldProvider() {
        return _activeWorldRenderer.getWorldProvider();
    }


}

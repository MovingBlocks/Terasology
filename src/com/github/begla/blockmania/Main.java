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
package com.github.begla.blockmania;

import com.github.begla.blockmania.player.Player;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.HeightMapFrame;
import com.github.begla.blockmania.utilities.Helper;
import com.github.begla.blockmania.utilities.VectorPool;
import com.github.begla.blockmania.world.Chunk;
import com.github.begla.blockmania.world.Primitives;
import com.github.begla.blockmania.world.World;
import com.github.begla.webupdater.WebUpdater;
import javolution.util.FastList;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Main {

    /* ------- */
    private static final int TICKS_PER_SECOND = 60;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    private static final int MAX_FRAMESKIP = 10;
    /* ------- */
    private static TrueTypeFont _font1;
    private long _lastLoopTime;
    private long _lastFpsTime;
    private int _fps;
    private final StringBuffer _consoleInput = new StringBuffer();
    private boolean _pauseGame = false;
    private boolean _runGame = true;
    private boolean _saveWorldOnExit = true;
    /* ------- */
    private float _meanFps;
    private float _memoryUsage;
    /* ------- */
    private Player _player;
    private World _world;
    /* ------- */
    private final FastRandom _rand = new FastRandom();

    /**
     * Entry point of the application.
     *
     * @param args Arguments
     */
    public static void main(String[] args) {

        Helper.LOGGER.log(Level.INFO, "Welcome to {0}!", Configuration.GAME_TITLE);

        /*
        * Update missing game files...
        */
        WebUpdater wu = new WebUpdater();

        if (!wu.update()) {
            Helper.LOGGER.log(Level.SEVERE, "Couldn't download missing game files. Sorry.");
            System.exit(0);
        }

        Main main = null;

        try {
            main = new Main();
            main.create();
            main.start();
        } catch (Exception ex) {
            Helper.LOGGER.log(Level.SEVERE, ex.toString(), ex);
        } finally {
            if (main != null) {
                main.destroy();
            }
        }

        System.exit(0);
    }

    /**
     * Init. the display and mouse/keyboard input.
     *
     * @throws LWJGLException
     */
    private void create() throws LWJGLException {
        Helper.LOGGER.log(Level.INFO, "Loading Blockmania. Please stand by...");

        // Display
        if (Configuration.FULLSCREEN) {
            Display.setDisplayMode(Display.getDesktopDisplayMode());
            Display.setFullscreen(true);
        } else {
            Display.setDisplayMode(Configuration.DISPLAY_MODE);
        }

        Display.setTitle(Configuration.GAME_TITLE);
        Display.create(Configuration.PIXEL_FORMAT);

        // Keyboard
        Keyboard.create();
        Keyboard.enableRepeatEvents(true);

        // Mouse
        Mouse.setGrabbed(true);
        Mouse.create();

        // OpenGL
        initGL();
        resizeGL();
    }

    /**
     * Clean up before exiting the application.
     */
    private void destroy() {
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    /**
     * Initializes OpenGL.
     */
    private void initGL() {
        // Init. fonts
        _font1 = new TrueTypeFont(new Font("Arial", Font.PLAIN, 12), true);

        /*
         * Load shaders.
         */
        ShaderManager.getInstance();

        /*
         * Init. OpenGL
         */
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

        Chunk.init();
        World.init();

        /*
         * Init. player and world
         */
        _player = new Player();
        // Generate a world with a random seed value
        String worldSeed = Configuration.DEFAULT_SEED;

        if (worldSeed.length() == 0) {
            worldSeed = _rand.randomCharacterString(16);
        }

        initNewWorld("World1", worldSeed);
    }

    /**
     * Renders the scene.
     */
    private void render() {

        // Fog has the same color as the sky
        float[] fogColor = {_world.getDaylight(), _world.getDaylight(), _world.getDaylight(), 1.0f};
        FloatBuffer fogColorBuffer = BufferUtils.createFloatBuffer(4);
        fogColorBuffer.put(fogColor);
        fogColorBuffer.rewind();
        glFog(GL_FOG_COLOR, fogColorBuffer);
        glFogi(GL_FOG_MODE, GL_LINEAR);

        // Update the viewing distance
        float minDist = Math.min(Configuration.getSettingNumeric("V_DIST_X") * Configuration.CHUNK_DIMENSIONS.x, Configuration.getSettingNumeric("V_DIST_Z") * Configuration.CHUNK_DIMENSIONS.z);
        float viewingDistance = minDist / 2f;
        glFogf(GL_FOG_START, 16f);
        glFogf(GL_FOG_END, viewingDistance);

        /*
         * Render the player, world and HUD.
         */
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        /**
         * Sky box.
         */
        glRotatef((float) _player.getPitch(), 1f, 0f, 0f);
        glRotatef((float) _player.getYaw(), 0f, 1f, 0f);

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_QUADS);
        Primitives.drawSkyBox(_world.getDaylight());
        glEnd();
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        glLoadIdentity();

        _player.render();
        _world.render();

        glBindTexture(GL_TEXTURE_2D, 0);
        ShaderManager.getInstance().enableShader(null);

        renderHUD();
    }

    /**
     * Resizes the viewport according to the chosen display width and height.
     */
    private void resizeGL() {
        glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(74.0f, (float) Display.getDisplayMode().getWidth() / (float) Display.getDisplayMode().getHeight(), 0.2f, 1024f);
        glPushMatrix();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
    }

    /**
     * Starts the render loop.
     */
    private void start() {
        Helper.LOGGER.log(Level.INFO, "Starting Blockmania...");
        _lastLoopTime = Helper.getInstance().getTime();

        double nextGameTick = Helper.getInstance().getTime();
        int loopCounter;

        /*
         * Main game loop.
         */
        while (_runGame && !Display.isCloseRequested()) {
            updateStatistics();
            processKeyboardInput();
            processMouseInput();

            // Pause the game while the debug console is being shown
            loopCounter = 0;
            while (Helper.getInstance().getTime() > nextGameTick && loopCounter < MAX_FRAMESKIP) {
                if (!_pauseGame) {
                    update();
                }
                nextGameTick += SKIP_TICKS;
                loopCounter++;
            }
            render();

            // Clear dirty flag and swap buffer
            Display.update();
        }

        /*
         * Save the world and exit the application.
         */
        if (_saveWorldOnExit) {
            _world.dispose();
        }
        Display.destroy();
    }

    /**
     * Updates the player and world.
     */
    private void update() {
        _world.update();
        _player.update();
    }

    /**
     * Renders the HUD on the screen.
     */
    private void renderHUD() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);

        // Draw the crosshair
        if (Configuration.getSettingBoolean("CROSSHAIR")) {
            glColor3f(1f, 1f, 1f);
            glLineWidth(2f);

            glBegin(GL_LINES);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f - 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f + 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f - 8f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f + 8f);
            glEnd();
        }

        /*
         * Draw debugging information.
         */
        if (Configuration.getSettingBoolean("DEBUG")) {
            _font1.drawString(4, 4, String.format("%s (fps: %.2f, mem usage: %.2f MB)", Configuration.GAME_TITLE, _meanFps, _memoryUsage));
            _font1.drawString(4, 22, String.format("%s", _player));
            _font1.drawString(4, 38, String.format("%s", _world));
            _font1.drawString(4, 54, String.format("total vus: %s", Chunk.getVertexArrayUpdateCount()));
        }

        if (_pauseGame) {
            // Display the console input text
            _font1.drawString(4, Display.getDisplayMode().getHeight() - 16 - 4, String.format("%s_", _consoleInput), Color.red);
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    /*
     * Process mouse input.
     */
    private void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            _player.processMouseInput(button, Mouse.getEventButtonState());
        }
    }

    /**
     * Processes keyboard input.
     */
    private void processKeyboardInput() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                toggleDebugConsole();
            }

            if (_pauseGame) {
                if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    if (key == Keyboard.KEY_BACK) {
                        int length = _consoleInput.length() - 1;

                        if (length < 0) {
                            length = 0;
                        }
                        _consoleInput.setLength(length);

                    } else if (key == Keyboard.KEY_RETURN) {
                        processConsoleString();
                    }

                    char c = Keyboard.getEventCharacter();

                    if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'A' + 1 || c == ' ' || c == '_' || c == '.' || c == '!') {
                        _consoleInput.append(c);
                    }
                }
            } else {
                _player.processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
            }
        }
    }

    /**
     * Parses the console string and executes the command.
     */
    private void processConsoleString() {
        boolean success = false;

        FastList<String> parsingResult = new FastList<String>();
        String temp = "";

        for (int i = 0; i < _consoleInput.length(); i++) {
            char c = _consoleInput.charAt(i);

            if (c != ' ') {
                temp = temp.concat(String.valueOf(c));
            }

            if (c == ' ' || i == _consoleInput.length() - 1) {
                parsingResult.add(temp);
                temp = "";
            }
        }

        // Try to parse the input
        try {
            if (parsingResult.get(0).equals("place")) {
                if (parsingResult.get(1).equals("tree")) {
                    _player.plantTree(Integer.parseInt(parsingResult.get(2)));
                    success = true;
                } else if (parsingResult.get(1).equals("block")) {
                    _player.placeBlock(Byte.parseByte(parsingResult.get(2)));
                    success = true;
                }
            } else if (parsingResult.get(0).equals("set")) {
                if (parsingResult.get(1).equals("time")) {
                    _world.setTime(Float.parseFloat(parsingResult.get(2)));
                    success = true;
                    // Otherwise try lookup the given variable within the settings
                } else {

                    Boolean bRes = Configuration.getSettingBoolean(parsingResult.get(1).toUpperCase());

                    if (bRes != null) {
                        Configuration.setSetting(parsingResult.get(1).toUpperCase(), Boolean.parseBoolean(parsingResult.get(2)));
                        success = true;
                    } else {
                        Float fRes = Configuration.getSettingNumeric(parsingResult.get(1).toUpperCase());
                        if (fRes != null) {
                            Configuration.setSetting(parsingResult.get(1).toUpperCase(), Float.parseFloat(parsingResult.get(2)));
                            success = true;
                        }
                    }
                }

            } else if (parsingResult.get(0).equals("respawn")) {
                _world.resetPlayer();
                success = true;
            } else if (parsingResult.get(0).equals("goto")) {
                int x = Integer.parseInt(parsingResult.get(1));
                int y = Integer.parseInt(parsingResult.get(2));
                int z = Integer.parseInt(parsingResult.get(3));
                _player.setPosition(VectorPool.getVector(x, y, z));
                success = true;
            } else if (parsingResult.get(0).equals("exit")) {
                _saveWorldOnExit = true;
                _runGame = false;
                success = true;
            } else if (parsingResult.get(0).equals("exit!")) {
                _saveWorldOnExit = false;
                _runGame = false;
                success = true;
            } else if (parsingResult.get(0).equals("info")) {
                Helper.LOGGER.log(Level.INFO, _player.selectedBlockInformation());
                success = true;
            } else if (parsingResult.get(0).equals("load")) {
                String worldSeed = _rand.randomCharacterString(16);

                if (parsingResult.size() > 1) {
                    worldSeed = parsingResult.get(1);
                }

                initNewWorld(worldSeed, worldSeed);
                success = true;
            } else if (parsingResult.get(0).equals("chunk_pos")) {
                _world.printPlayerChunkPosition();
                success = true;
            } else if (parsingResult.get(0).equals("map")) {
                new HeightMapFrame(_world).setVisible(true);
                success = true;
            } else if (parsingResult.get(0).equals("update_all")) {
                _world.updateAllChunks();
                success = true;
            } else if (parsingResult.get(0).equals("set_spawn")) {
                _world.setSpawningPoint();
                success = true;
            }
        } catch (Exception e) {
            Helper.LOGGER.log(Level.INFO, e.getMessage());
        }

        if (success) {
            Helper.LOGGER.log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);
        } else {
            Helper.LOGGER.log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
        }

        toggleDebugConsole();
    }

    /**
     * Disables/enables the debug console.
     */
    private void toggleDebugConsole() {
        if (!_pauseGame) {
            _world.suspendUpdateThread();
            _consoleInput.setLength(0);
            _pauseGame = true;
        } else {
            _pauseGame = false;
            _world.resumeUpdateThread();
        }
    }

    /**
     * Prepares a new world with a given name and seed value.
     *
     * @param title Title of the world
     * @param seed  Seed value used for the generators
     */
    private void initNewWorld(String title, String seed) {
        Helper.LOGGER.log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Get rid of the old world
        if (_world != null) {
            _world.dispose();
        }

        // Init some world
        _world = new World(title, seed, _player);
        // Link the player to the world
        _player.setParent(_world);

        _world.startUpdateThread();

        Helper.LOGGER.log(Level.INFO, "Waiting for some chunks to pop up...", seed);
        while (_world.getAmountGeneratedChunks() < 64) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ex) {
                Helper.LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        // Reset the delta value
        _lastLoopTime = Helper.getInstance().getTime();
    }

    /**
     * Updates the game statistics like FPS and memory usage.
     */
    private void updateStatistics() {
        // Measure a delta value and the frames per second
        long delta = Helper.getInstance().getTime() - _lastLoopTime;
        _lastLoopTime = Helper.getInstance().getTime();
        _lastFpsTime += delta;
        _fps++;

        // Update the FPS and calculate the mean for displaying
        if (_lastFpsTime >= 1000) {
            _lastFpsTime = 0;

            _meanFps += _fps;
            _meanFps /= 2;

            // Calculate the current memory usage in MB
            _memoryUsage = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1048576;

            _fps = 0;
        }
    }
}

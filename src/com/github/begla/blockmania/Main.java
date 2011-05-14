/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania;

import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.Helper;
import java.awt.Font;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Main {

    private static final Logger _logger = Logger.getLogger(Main.class.getName());
    /* ------- */
    private static TrueTypeFont _font1;
    private long _lastLoopTime;
    private long _lastFpsTime;
    private int _fps;
    private final StringBuffer _consoleInput = new StringBuffer();
    private boolean _showDebugConsole = false;
    private boolean _runGame = true;
    /* ------- */
    private float _meanFps;
    /* ------- */
    Player _player;
    World _world;
    /* ------- */
    FastRandom _rand = new FastRandom();

    /**
     * Init. the logger.
     */
    static {
        try {
            _logger.addHandler(new FileHandler("blockmania.log", true));
        } catch (IOException ex) {
            _logger.log(Level.WARNING, ex.toString(), ex);
        }
    }

    /**
     * Entry point of the application.
     * 
     * @param args Arguments
     */
    public static void main(String[] args) {

        _logger.log(Level.INFO, "Welcome to {0}!", Configuration.GAME_TITLE);

        Main main = null;

        try {
            main = new Main();
            main.create();
            main.start();
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, ex.toString(), ex);
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
        _logger.log(Level.INFO, "Initializing display, input devices and OpenGL.");

        // Display
        Display.setDisplayMode(new DisplayMode(Configuration.DISPLAY_WIDTH, Configuration.DISPLAY_HEIGHT));
        Display.setFullscreen(Configuration.FULLSCREEN);
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
        // Init. the fonts
        _font1 = new TrueTypeFont(new Font("Arial", Font.PLAIN, 12), true);

        // Init. OpenGL
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_FOG);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glPolygonOffset(0.1f, 0.1f);
        glEnable(GL_POLYGON_OFFSET_FILL);

        // Enable fog
        glHint(GL_FOG_HINT, GL_NICEST);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        float viewingDistance = (Configuration.VIEWING_DISTANCE_IN_CHUNKS.x * Configuration.CHUNK_DIMENSIONS.x) / 2f;
        glFogf(GL_FOG_START, viewingDistance / 16f);
        glFogf(GL_FOG_END, viewingDistance);

        Chunk.init();
        World.init();

        // Init. the player and a world
        _player = new Player();
        // Generate a world with a "random" seed value
        String worldSeed = Configuration.DEFAULT_SEED;
        if (worldSeed.length() == 0) {
            worldSeed = _rand.randomCharacterString(16);
        }
        initNewWorld("World1", worldSeed);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        _player.resetPlayer();
    }

    /**
     * Renders the scene, player and HUD.
     */
    private void render() {
        // Use the color of the sky for clearing
        glClearColor(_world.getDaylightColor().x, _world.getDaylightColor().y, _world.getDaylightColor().z, 1.0f);

        // Color the fog like the sky
        float[] fogColor = {_world.getDaylightColor().x, _world.getDaylightColor().y, _world.getDaylightColor().z, 1.0f};
        FloatBuffer fogColorBuffer = BufferUtils.createFloatBuffer(4);
        fogColorBuffer.put(fogColor);
        fogColorBuffer.rewind();
        glFog(GL_FOG_COLOR, fogColorBuffer);

        /*
         * Render the player, world and HUD.
         */
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        _player.render();
        _world.render();

        if (Configuration.getSettingBoolean("SHOW_HUD")) {
            renderHUD();
        }
    }

    /**
     * Resizes the viewport according to the chosen display with and height.
     */
    private void resizeGL() {
        glViewport(0, 0, Configuration.DISPLAY_WIDTH, Configuration.DISPLAY_HEIGHT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(84.0f, (float) Configuration.DISPLAY_WIDTH / (float) Configuration.DISPLAY_HEIGHT, 0.01f, 256);
        glPushMatrix();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
    }

    /**
     * Starts the render loop. The application can be terminated by pressing
     * the ESCAPE key.
     */
    private void start() {
        _logger.log(Level.INFO, "Starting the game...");
        _lastLoopTime = Helper.getInstance().getTime();
        while (_runGame) {

            // Sync. at 60 FPS
            Display.sync(120);

            // Measure a delta value and the frames per second
            long delta = Helper.getInstance().getTime() - _lastLoopTime;
            _lastLoopTime = Helper.getInstance().getTime();
            _lastFpsTime += delta;
            _fps++;

            // Updates the FPS and calculate the mean for display
            if (_lastFpsTime >= 1000) {
                _lastFpsTime = 0;

                _meanFps += _fps;
                _meanFps /= 2;

                _fps = 0;
            }

            processKeyboardInput();
            processMouseInput();

            /*
             * Updating and rendering of the scene. The delta
             * value is used within the updating process.
             */
            if (!_showDebugConsole) {
                // Pause the game while the debug console is being shown
                update(delta);
            }
            render();

            // Clear dirty flag and swap buffer
            Display.update();
        }

        Display.destroy();
    }

    /**
     * Updates the player and the world.
     */
    private void update(long delta) {
        _world.update(delta);
        _player.update(delta);
    }

    /**
     * Renders the HUD on the screen.
     */
    private void renderHUD() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Display.getDisplayMode().getWidth(),
                Display.getDisplayMode().getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);

        // Draw debugging information
        _font1.drawString(4, 4, String.format("%s (fps: %.2f, free heap space: %d MB)", Configuration.GAME_TITLE, _meanFps, Runtime.getRuntime().freeMemory() / 1048576), Color.white);
        _font1.drawString(4, 22, String.format("%s", _player, Color.white));
        _font1.drawString(4, 38, String.format("%s", _world, Color.white));
        _font1.drawString(4, 54, String.format("total vus: %s", Chunk.getVertexArrayUpdateCount(), Color.white));

        if (_showDebugConsole) {
            // Display the console input text
            _font1.drawString(4, Configuration.DISPLAY_HEIGHT - 16 - 4, String.format("%s_", _consoleInput), Color.red);
        }


        glDisable(GL_TEXTURE_2D);

        glColor3f(1f, 1f, 1f);
        glLineWidth(2f);
        // Draw the crosshair
        glBegin(GL_LINES);
        glVertex2d(Display.getDisplayMode().getWidth() / 2f - 8f, Display.getDisplayMode().getHeight() / 2f);
        glVertex2d(Display.getDisplayMode().getWidth() / 2f + 8f, Display.getDisplayMode().getHeight() / 2f);

        glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f - 8f);
        glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f + 8f);
        glEnd();


        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glLoadIdentity();
    }

    private void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            _player.processMouseInput(button, Mouse.getEventButtonState());
        }
    }

    /**
     * Processes the keyboard input.
     */
    private void processKeyboardInput() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                toggleDebugConsole();
            }

            if (_showDebugConsole) {
                if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    if (key == Keyboard.KEY_BACK) {
                        try {
                            _consoleInput.setLength(_consoleInput.length() - 1);
                        } catch (Exception e) {
                        }
                    } else if (key == Keyboard.KEY_RETURN) {
                        processConsoleString();
                    }

                    char c = Keyboard.getEventCharacter();

                    try {
                        if (c >= 'a' && c < 'z' || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'A' + 1 || c == ' ' || c == '_' || c == '.' || c == '!') {
                            _consoleInput.append(c);
                        }
                    } catch (Exception e) {
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

        ArrayList<String> parsingResult = new ArrayList<String>();
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
                    _world.setTime(Short.parseShort(parsingResult.get(2)));
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

            } else if (parsingResult.get(0).equals("reset_player")) {
                _player.resetPlayer();
                success = true;
            } else if (parsingResult.get(0).equals("goto")) {
                int x = Integer.parseInt(parsingResult.get(1));
                int y = Integer.parseInt(parsingResult.get(2));
                int z = Integer.parseInt(parsingResult.get(3));
                _player.setPosition(new Vector3f(x, y, z));
                success = true;
            } else if (parsingResult.get(0).equals("exit")) {
                _world.dispose();
                System.exit(0);
                success = true;
            } else if (parsingResult.get(0).equals("exit!")) {
                System.exit(0);
                success = true;
            } else if (parsingResult.get(0).equals("i")) {
                _logger.log(Level.INFO, _player.selectedBlockInformation());
                success = true;
            } else if (parsingResult.get(0).equals("generate_new_world")) {
                String worldSeed = _rand.randomCharacterString(16);

                if (parsingResult.size() > 1) {
                    worldSeed = parsingResult.get(1);
                }

                initNewWorld("", worldSeed);
                success = true;
            } else if (parsingResult.get(0).equals("chunk_information")) {
                _world.printDirtyChunks();
                _world.printLightDirtyChunks();
                _world.printFreshChunks();
                success = true;
            } else if (parsingResult.get(0).equals("chunk_pos")) {
                _world.printPlayerChunkPosition();
                success = true;
            }
        } catch (Exception e) {
        }

        if (success) {
            _logger.log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);
        } else {
            _logger.log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
        }

        toggleDebugConsole();
    }

    /**
     * Disables/enables the debug console.
     */
    public void toggleDebugConsole() {
        if (!_showDebugConsole) {
            _world.suspendUpdateThread();
            _consoleInput.setLength(0);
            _showDebugConsole = true;
        } else {
            _showDebugConsole = false;
            _world.resumeUpdateThread();
        }
    }

    public void initNewWorld(String title, String seed) {
        _logger.log(Level.INFO, "Creating new World with seed \"{0}\"", seed);
        if (_world != null) {
            _world.dispose();
        }
        _world = new World(title, seed, _player);
        // Link the player to the world
        _player.setParent(_world);
        _world.startUpdateThread();
        for (int i = 0; i < 15; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.printf("Waiting for some chunks to pop up... %.3f%%\n", ((i + 1) / 15f) * 100f);
        }

        _player.resetPlayer();
    }
}

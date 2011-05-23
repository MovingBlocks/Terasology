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
 */
package com.github.begla.blockmania;

import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.HeightMapFrame;
import com.github.begla.blockmania.utilities.VectorPool;
import java.awt.Font;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.nio.FloatBuffer;
import java.util.logging.Level;
import javolution.util.FastList;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.NVFogDistance;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Main {


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
    private float _memoryUsage;
    /* ------- */
    Player _player;
    World _world;
    /* ------- */
    FastRandom _rand = new FastRandom();

    /**
     * Entry point of the application.
     * 
     * @param args Arguments
     */
    public static void main(String[] args) {

        Helper.LOGGER.log(Level.INFO, "Welcome to {0}!", Configuration.GAME_TITLE);

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
        Helper.LOGGER.log(Level.INFO, "Initializing display, input devices and OpenGL.");

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
        // Init. the fonts
        _font1 = new TrueTypeFont(new Font("Arial", Font.PLAIN, 12), true);

        // Init. OpenGL
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_FOG);
        glDepthFunc(GL_LEQUAL);

        if (GLContext.getCapabilities().GL_NV_fog_distance) {
            glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV);
            Helper.LOGGER.log(Level.INFO, "Extension 'GL_NV_fog_distance' is supported.");
        }

        //glPolygonOffset(0.1f, 0.1f);
        //glEnable(GL_POLYGON_OFFSET_FILL);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_FOG_HINT, GL_NICEST);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

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


        // Update by the viewing distance
        float maxDist = Math.max(Configuration.getSettingNumeric("V_DIST_X") * Configuration.CHUNK_DIMENSIONS.x, Configuration.getSettingNumeric("V_DIST_Z") * Configuration.CHUNK_DIMENSIONS.z);
        float viewingDistance = maxDist / 2f;
        glFogf(GL_FOG_START, 32f);
        glFogf(GL_FOG_END, viewingDistance);

        /*
         * Render the player, world and HUD.
         */
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        _player.render();
        _world.render();

        renderHUD();
    }

    /**
     * Resizes the viewport according to the chosen display with and height.
     */
    private void resizeGL() {
        glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(84.0f, (float) Display.getDisplayMode().getWidth() / (float) Display.getDisplayMode().getHeight(), 0.1f, 512f);
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
        Helper.LOGGER.log(Level.INFO, "Starting the game...");
        _lastLoopTime = Helper.getInstance().getTime();
        while (_runGame) {

            // Sync. at 60 FPS
            Display.sync(60);

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

                // Calculate the current memory usage in MB
                _memoryUsage = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1048576;

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
        glOrtho(0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_FOG);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);

        if (Configuration.getSettingBoolean("SHOW_DEBUG_INFORMATION")) {
            // Draw debugging information
            _font1.drawString(4, 4, String.format("%s (fps: %.2f, mem usage: %.2f MB)", Configuration.GAME_TITLE, _meanFps, _memoryUsage, Color.white));
            _font1.drawString(4, 22, String.format("%s", _player, Color.white));
            _font1.drawString(4, 38, String.format("%s", _world, Color.white));
            _font1.drawString(4, 54, String.format("total vus: %s", Chunk.getVertexArrayUpdateCount(), Color.white));
        }

        if (_showDebugConsole) {
            // Display the console input text
            _font1.drawString(4, Display.getDisplayMode().getHeight() - 16 - 4, String.format("%s_", _consoleInput), Color.red);
        }


        glDisable(GL_TEXTURE_2D);

        glColor3f(1f, 1f, 1f);
        glLineWidth(2f);

        // Draw the crosshair
        if (Configuration.getSettingBoolean("SHOW_CROSSHAIR")) {
            glBegin(GL_LINES);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f - 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f + 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f - 8f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f + 8f);
            glEnd();
        }

        glDisable(GL_BLEND);
        glEnable(GL_FOG);
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
                _world.dispose();
                System.exit(0);
                success = true;
            } else if (parsingResult.get(0).equals("exit!")) {
                System.exit(0);
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
            } else if (parsingResult.get(0).equals("show_map")) {
                new HeightMapFrame(_world).setVisible(true);
                success = true;
            } else if (parsingResult.get(0).equals("check")) {
                _world.checkVisibleChunks();
                success = true;
            } else if (parsingResult.get(0).equals("update_all")) {
                _world.updateAllChunks();
                success = true;
            }
        } catch (IndexOutOfBoundsException e) {
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
        if (!_showDebugConsole) {
            _world.suspendUpdateThread();
            _consoleInput.setLength(0);
            _showDebugConsole = true;
        } else {
            _showDebugConsole = false;
            _world.resumeUpdateThread();
        }
    }

    /**
     * 
     * @param title
     * @param seed
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
        while (_world.getStatGeneratedChunks() < 16) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Helper.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        Helper.LOGGER.log(Level.INFO, "Finished!", seed);

        // Reset the delta value
        _lastLoopTime = Helper.getInstance().getTime();
    }
}

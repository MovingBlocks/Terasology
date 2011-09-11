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

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.rendering.ShaderManager;
import com.github.begla.blockmania.rendering.VectorPool;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.Chunk;
import com.github.begla.blockmania.world.Player;
import com.github.begla.blockmania.world.World;
import javolution.util.FastList;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Game {

    /* ------- */
    private static final int TICKS_PER_SECOND = 120;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
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
    private double _meanFps;
    private double _memoryUsage;
    /* ------- */
    private Player _player;
    private World _world;
    /* ------- */
    private final FastRandom _rand = new FastRandom();
    /* ------- */
    private long _timerTicksPerSecond;
    /* ------- */
    private static Game _instance;
    /* ------- */
    private final Logger _logger = Logger.getLogger("blockmania");
    /* ------- */
    private boolean _sandbox = false;
    /* ------- */
    private double _cubeRotation;

    // Singleton
    public static Game getInstance() {
        if (_instance == null)
            _instance = new Game();
        return _instance;
    }

    /**
     * Entry point of the application.
     *
     * @param args Arguments
     */
    public static void main(String[] args) {

        Game.getInstance().addLogFileHandler("blockmania.log");
        Game.getInstance().getLogger().log(Level.INFO, "Welcome to {0}!", Configuration.GAME_TITLE);

        /*
        * Update missing game files...
        */
        // WebUpdater wu = new WebUpdater();

        try {
            loadLibs();
        } catch (Exception e) {
            Game.getInstance().getLogger().log(Level.SEVERE, "Couldn't link static libraries. Sorry: " + e);
        }

/*        if (!wu.update()) {
            Game.getInstance().getLogger().log(Level.SEVERE, "Couldn't download missing game files. Sorry.");
            System.exit(0);
        }*/

        Game game = null;

        try {
            game = Game.getInstance();

            game.initDisplay();
            game.initControls();

            game.initGame();

            game.startGame();
        } catch (Exception ex) {
            Game.getInstance().getLogger().log(Level.SEVERE, ex.toString(), ex);
        } finally {
            if (game != null) {
                game.destroy();
            }
        }

        System.exit(0);
    }

    /**
     * Returns the system time in milliseconds.
     *
     * @return The system time in milliseconds.
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }

    private static void loadLibs() throws Exception {
        if (System.getProperty("os.name").equals("Mac OS X"))
            addLibraryPath("natives/macosx");
        else if (System.getProperty("os.name").equals("Linux"))
            addLibraryPath("natives/linux");
        else
            addLibraryPath("natives/windows");
    }

    private static void addLibraryPath(String s) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        final String[] paths = (String[]) usrPathsField.get(null);

        for (String path : paths) {
            if (path.equals(s)) {
                return;
            }
        }

        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = s;
        usrPathsField.set(null, newPaths);
    }

    /**
     * Init. the display and mouse/keyboard input.
     *
     * @throws LWJGLException
     */
    public void initDisplay() throws LWJGLException {
        Game.getInstance().getLogger().log(Level.INFO, "Loading Blockmania. Please stand by...");

        // Display
        if (Configuration.FULLSCREEN) {
            Display.setDisplayMode(Display.getDesktopDisplayMode());
            Display.setFullscreen(true);
        } else {
            Display.setDisplayMode(Configuration.DISPLAY_MODE);
        }

        Display.setTitle(Configuration.GAME_TITLE);
        Display.create(Configuration.PIXEL_FORMAT);

    }

    /**
     * @throws LWJGLException
     */
    public void initControls() throws LWJGLException {
        // Keyboard
        Keyboard.create();
        Keyboard.enableRepeatEvents(true);

        // Mouse
        Mouse.setGrabbed(true);
        Mouse.create();
    }

    /**
     * Clean up before exiting the application.
     */
    public void destroy() {
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    public void initGame() {
        _timerTicksPerSecond = Sys.getTimerResolution();
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
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        glShadeModel(GL11.GL_SMOOTH);

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
        glFogi(GL_FOG_MODE, GL_LINEAR);
        // Update the viewing distance
        double minDist = Math.min(Configuration.getSettingNumeric("V_DIST_X") * Configuration.CHUNK_DIMENSIONS.x, Configuration.getSettingNumeric("V_DIST_Z") * Configuration.CHUNK_DIMENSIONS.z);
        double viewingDistance = minDist / 2f;
        glFogf(GL_FOG_START, (float) (viewingDistance * 0.05));
        glFogf(GL_FOG_END, (float) viewingDistance);

        /*
         * Render the player, world and HUD.
         */
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        _world.render();

        renderHUD();
    }

    /**
     * Resizes the viewport according to the chosen display width and height.
     */
    private void resizeViewport() {
        glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(80.0f, (float) Display.getDisplayMode().getWidth() / (float) Display.getDisplayMode().getHeight(), 0.1f, 756f);
        glPushMatrix();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
    }

    /**
     * Starts the render loop.
     */
    public void startGame() {
        Game.getInstance().getLogger().log(Level.INFO, "Starting Blockmania...");
        _lastLoopTime = getTime();

        double nextGameTick = getTime();
        int loopCounter;

        resizeViewport();

        /*
         * Game game loop.
         */
        while (_runGame && !Display.isCloseRequested()) {
            updateStatistics();
            processKeyboardInput();
            processMouseInput();

            // Pause the game while the debug console is being shown
            loopCounter = 0;
            while (getTime() > nextGameTick && loopCounter < Configuration.FRAME_SKIP_MAX_FRAMES) {
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

    public void stopGame() {
        _runGame = false;
    }

    public void pauseGame() {
        _world.suspendUpdateThread();
        Mouse.setGrabbed(false);
        _pauseGame = true;
    }

    public void unpauseGame() {
        _pauseGame = false;
        Mouse.setGrabbed(true);
        _world.resumeUpdateThread();
    }

    /**
     * Updates the world.
     */
    private void update() {
        _cubeRotation += 0.5;
        _world.update();
    }

    private void drawRotatingBlock() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        gluPerspective(25f, (float) Display.getDisplayMode().getWidth() / (float) Display.getDisplayMode().getHeight(), 0.1f, 32f);
        glMatrixMode(GL_MODELVIEW);

        glLoadIdentity();

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glAlphaFunc(GL_GREATER, 0.1f);
        glDisable(GL_DEPTH_TEST);
        gluLookAt(0, 0, -25, 8f, 4.5f, 0, 0, 1, 0);
        glRotated(_cubeRotation % 360, 0, 1, 1);
        Block.getBlockForType(_player.getSelectedBlockType()).renderBlock(true);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL11.GL_BLEND);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    /**
     * Renders the HUD on the screen.
     */
    private void renderHUD() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0, -5, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        // Draw the crosshair
        if (Configuration.getSettingBoolean("CROSSHAIR")) {
            glColor4f(1f, 1f, 1f, 1f);
            glLineWidth(2f);

            glBegin(GL_LINES);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f - 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f + 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f - 8f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f + 8f);
            glEnd();
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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

        if (Configuration.getSettingBoolean("ROTATING_BLOCK")) {
            drawRotatingBlock();
        }
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

                    if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'A' + 1 || c == ' ' || c == '_' || c == '.' || c == '!' || c == '-') {
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
                        Double fRes = Configuration.getSettingNumeric(parsingResult.get(1).toUpperCase());
                        if (fRes != null) {
                            Configuration.setSetting(parsingResult.get(1).toUpperCase(), Double.parseDouble(parsingResult.get(2)));
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
                Game.getInstance().getLogger().log(Level.INFO, _player.selectedBlockInformation());
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
            } else if (parsingResult.get(0).equals("set_spawn")) {
                _world.setSpawningPoint();
                success = true;
            }
        } catch (Exception e) {
            Game.getInstance().getLogger().log(Level.INFO, e.getMessage());
        }

        if (success) {
            Game.getInstance().getLogger().log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);
        } else {
            Game.getInstance().getLogger().log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
        }

        toggleDebugConsole();
    }

    /**
     * Disables/enables the debug console.
     */
    private void toggleDebugConsole() {
        if (!_pauseGame) {
            pauseGame();
            _consoleInput.setLength(0);

        } else {
            unpauseGame();
        }
    }

    /**
     * Prepares a new world with a given name and seed value.
     *
     * @param title Title of the world
     * @param seed  Seed value used for the generators
     */
    private void initNewWorld(String title, String seed) {
        Game.getInstance().getLogger().log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Get rid of the old world
        if (_world != null) {
            _world.dispose();
        }

        // Init some world
        _world = new World(title, seed, _player);
        // Link the player to the world
        _player.setParent(_world);

        _world.startUpdateThread();

        // Reset the delta value
        _lastLoopTime = getTime();
    }

    /**
     * Updates the game statistics like FPS and memory usage.
     */
    private void updateStatistics() {
        // Measure a delta value and the frames per second
        long delta = getTime() - _lastLoopTime;
        _lastLoopTime = getTime();
        _lastFpsTime += delta;
        _fps++;

        // Update the FPS and calculate the average
        if (_lastFpsTime >= 1000) {
            _lastFpsTime = 0;

            _meanFps += _fps;
            _meanFps /= 2;

            // Calculate the current memory usage in MB
            _memoryUsage = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1048576;

            _fps = 0;
        }
    }

    public void addLogFileHandler(String s) {
        try {
            FileHandler fh = new FileHandler(s, true);
            fh.setFormatter(new SimpleFormatter());
            _logger.addHandler(fh);
        } catch (IOException ex) {
            _logger.log(Level.WARNING, ex.toString(), ex);
        }
    }

    public Logger getLogger() {
        return _logger;
    }

    public void setSandboxed(boolean b) {
        _sandbox = b;
    }

    public boolean isSandboxed() {
        return _sandbox;
    }
}

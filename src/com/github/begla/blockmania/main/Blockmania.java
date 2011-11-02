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
package com.github.begla.blockmania.main;

import com.github.begla.blockmania.gui.HUD;
import com.github.begla.blockmania.rendering.FontManager;
import com.github.begla.blockmania.rendering.ShaderManager;
import com.github.begla.blockmania.rendering.VBOManager;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.World;
import com.github.begla.blockmania.world.characters.Player;
import javolution.util.FastList;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.SoundStore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.lwjgl.opengl.GL11.*;

/**
 * The heart and soul of Blockmania.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Blockmania {

    private final ThreadPoolExecutor _threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(32);
    /* ------ */
    private static final int FRAME_SKIP_MAX_FRAMES = 5;
    private static final int TICKS_PER_SECOND = 60;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    /* ------- */
    private long _lastLoopTime, _lastFpsTime;
    private int _fps;
    private final StringBuffer _consoleInput = new StringBuffer();
    private boolean _pauseGame = false, _runGame = true, _saveWorldOnExit = true;
    /* ------- */
    private double _averageFps;
    /* ------- */
    private World _world;
    private HUD _hud;
    /* ------- */
    private final FastRandom _rand = new FastRandom();
    /* ------- */
    private long _timerTicksPerSecond;
    /* ------- */
    private static Blockmania _instance;
    /* ------- */
    private final Logger _logger = Logger.getLogger("blockmania");

    // Singleton
    public static Blockmania getInstance() {
        if (_instance == null)
            _instance = new Blockmania();
        return _instance;
    }

    /**
     * Entry point of the application.
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        initDefaultLogger();
        Blockmania.getInstance().getLogger().log(Level.INFO, "Welcome to {0}!", Configuration.GAME_TITLE);

        try {
            loadNativeLibs();
        } catch (Exception e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Couldn't link static libraries. Sorry. " + e.toString(), e);
        }

        Blockmania blockmania = null;

        try {
            blockmania = Blockmania.getInstance();

            blockmania.initDisplay();
            blockmania.initControls();
            blockmania.initGame();

            blockmania.startGame();
        } catch (LWJGLException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to start game. I'm sorry. " + e.toString(), e);
        } catch (SlickException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to start game. I'm sorry. " + e.toString(), e);
        } finally {
            if (blockmania != null) {
                blockmania.destroy();
            }
        }

        System.exit(0);
    }

    private static void initDefaultLogger() {
        File dirPath = new File("logs");

        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                return;
            }
        }

        Blockmania.getInstance().addLogFileHandler("logs/blockmania.log", Level.INFO);
    }

    private static void loadNativeLibs() throws Exception {
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
     * Returns the system time in milliseconds.
     *
     * @return The system time in milliseconds.
     */
    public long getTime() {
        if (_timerTicksPerSecond == 0)
            return 0;

        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }

    /**
     * Init. the display.
     *
     * @throws LWJGLException
     */
    public void initDisplay() throws LWJGLException {
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
     * Init. keyboard and mouse input.
     *
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
        AL.destroy();
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    public void initGame() throws SlickException {
        _timerTicksPerSecond = Sys.getTimerResolution();

        /*
         * Init. GUI.
         */
        _hud = new HUD();

        /*
         * Init. management classes.
         */
        ShaderManager.getInstance();
        VBOManager.getInstance();
        FontManager.getInstance();

        /*
         * Init. OpenGL
         */
        resizeViewport();
        setupOpenGL();

        // Generate a world with a random seed value
        String worldSeed = Configuration.DEFAULT_SEED;

        if (worldSeed.length() == 0) {
            worldSeed = _rand.randomCharacterString(16);
        }

        initNewWorldAndPlayer("World1", worldSeed);
    }

    private void setupOpenGL() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        // Update the viewing distance
        double minDist = Math.min(Configuration.getSettingNumeric("V_DIST_X") * Configuration.CHUNK_DIMENSIONS.x, Configuration.getSettingNumeric("V_DIST_Z") * Configuration.CHUNK_DIMENSIONS.z) / 2.0;
        glFogf(GL_FOG_START, (float) (minDist * 0.5));
        glFogf(GL_FOG_END, (float) minDist);

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        glShadeModel(GL11.GL_SMOOTH);
    }

    /**
     * Renders the scene.
     */
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        _world.render();
        _hud.render();
    }

    private void resizeViewport() {
        glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
    }

    /**
     * Starts the render loop.
     */
    public void startGame() {
        Blockmania.getInstance().getLogger().log(Level.INFO, "Starting Blockmania...");
        _lastLoopTime = getTime();

        double nextGameTick = getTime();
        int loopCounter;

        /*
         * Blockmania game loop.
         */
        while (_runGame && !Display.isCloseRequested()) {
            updateStatistics();
            processKeyboardInput();
            processMouseInput();

            // Pause the game while the debug console is being shown
            loopCounter = 0;
            while (getTime() > nextGameTick && loopCounter < FRAME_SKIP_MAX_FRAMES) {
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

        _threadPool.shutdown();

        try {
            _threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        Display.destroy();
    }

    public void stopGame() {
        _runGame = false;
    }

    public void pauseGame() {
        Mouse.setGrabbed(false);
        _pauseGame = true;
    }

    public void unpauseGame() {
        _pauseGame = false;
        Mouse.setGrabbed(true);
    }

    /**
     * Executes updates.
     */
    private void update() {
        _hud.update();
        _world.update();

        // Important for the streaming of audio
        SoundStore.get().poll(0);
    }

    /*
     * Process mouse input.
     */
    private void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            _world.getPlayer().processMouseInput(button, Mouse.getEventButtonState());
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

            if (key == Keyboard.KEY_F3 && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                Configuration.setSetting("DEBUG", !Configuration.getSettingBoolean("DEBUG"));
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
                // Pass input to the current player
                _world.getPlayer().processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
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
                if (parsingResult.get(1).equals("block")) {
                    _world.getPlayer().placeBlock(Byte.parseByte(parsingResult.get(2)));
                    success = true;
                }
            } else if (parsingResult.get(0).equals("set")) {
                if (parsingResult.get(1).equals("time")) {
                    _world.getWorldProvider().setTime(Float.parseFloat(parsingResult.get(2)));
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
                _world.getPlayer().respawn();
                success = true;
            } else if (parsingResult.get(0).equals("goto")) {
                int x = Integer.parseInt(parsingResult.get(1));
                int y = Integer.parseInt(parsingResult.get(2));
                int z = Integer.parseInt(parsingResult.get(3));
                _world.getPlayer().setPosition(new Vector3f(x, y, z));
                success = true;
            } else if (parsingResult.get(0).equals("exit")) {
                _saveWorldOnExit = true;
                _runGame = false;
                success = true;
            } else if (parsingResult.get(0).equals("exit!")) {
                _saveWorldOnExit = false;
                _runGame = false;
                success = true;
            } else if (parsingResult.get(0).equals("load")) {
                String worldSeed = _rand.randomCharacterString(16);

                if (parsingResult.size() > 1) {
                    worldSeed = parsingResult.get(1);
                }

                initNewWorldAndPlayer(worldSeed, worldSeed);
                success = true;
            } else if (parsingResult.get(0).equals("set_spawn")) {
                _world.getPlayer().setSpawningPoint();
                success = true;
            }
        } catch (Exception e) {
            Blockmania.getInstance().getLogger().log(Level.INFO, e.getMessage());
        }

        if (success) {
            setupOpenGL();
            Blockmania.getInstance().getLogger().log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);
        } else {
            Blockmania.getInstance().getLogger().log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
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
    private void initNewWorldAndPlayer(String title, String seed) {
        Blockmania.getInstance().getLogger().log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Get rid of the old world
        if (_world != null) {
            _world.dispose();
        }

        // Init. a new world
        _world = new World(title, seed);
        _world.setPlayer(new Player(_world));

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

            _averageFps += _fps;
            _averageFps /= 2;

            _fps = 0;
        }
    }

    public void addLogFileHandler(String s, Level logLevel) {
        try {
            FileHandler fh = new FileHandler(s, true);
            fh.setLevel(logLevel);
            fh.setFormatter(new SimpleFormatter());
            _logger.addHandler(fh);
        } catch (IOException ex) {
            _logger.log(Level.WARNING, ex.toString(), ex);
        }
    }

    public Logger getLogger() {
        return _logger;
    }

    public boolean isGamePaused() {
        return _pauseGame;
    }

    public double getAverageFps() {
        return _averageFps;
    }

    public World getActiveWorld() {
        return _world;
    }

    public StringBuffer getConsoleInput() {
        return _consoleInput;
    }

    public ThreadPoolExecutor getThreadPool() {
        return _threadPool;
    }
}

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
package com.github.begla.blockmania.game;

import com.github.begla.blockmania.logic.characters.Player;
import com.github.begla.blockmania.logic.manager.*;
import com.github.begla.blockmania.logic.world.WorldProvider;
import com.github.begla.blockmania.model.blocks.BlockManager;
import com.github.begla.blockmania.rendering.gui.menus.UIHeadsUpDisplay;
import com.github.begla.blockmania.rendering.gui.menus.UIPauseMenu;
import com.github.begla.blockmania.rendering.gui.menus.UIStatusScreen;
import com.github.begla.blockmania.rendering.world.WorldRenderer;
import com.github.begla.blockmania.utilities.BlockmaniaProfiler;
import com.github.begla.blockmania.utilities.FastRandom;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
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

    /* VIEWING DISTANCE */
    private static final int[] VIEWING_DISTANCES = {(Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceNear"),
            (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceModerate"),
            (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceFar"),
            (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceUltra")};

    /* SETTINGS */
    private static final int FPS_LIMIT = (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.fpsLimit");

    private int _activeViewingDistance = 0;

    /* THREADING */
    private final ThreadPoolExecutor _threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /* CONST */
    private static final int FRAME_SKIP_MAX_FRAMES = 10;
    private static final int TICKS_PER_SECOND = 60;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;

    /* STATISTICS */
    private long _lastLoopTime, _lastFpsTime;
    private double _averageFps;
    private int _fps;
    private long _timerTicksPerSecond;

    /* GAME LOOP */
    private boolean _pauseGame = false, _runGame = true, _saveWorldOnExit = true;

    /* RENDERING */
    private WorldRenderer _activeWorldRenderer;

    /* GUI */
    private UIHeadsUpDisplay _hud;
    private UIPauseMenu _pauseMenu;
    private UIStatusScreen _statusScreen;

    /* SINGLETON */
    private static Blockmania _instance;

    /* LOGGING */
    private final Logger _logger;

    /* GROOVY */
    private GroovyManager _groovyManager;

    /**
     * Returns the static instance of Blockmania.
     *
     * @return The instance
     */
    public static Blockmania getInstance() {
        if (_instance == null)
            _instance = new Blockmania();
        return _instance;
    }

    /**
     * Entry point of the application.
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        getInstance().initDefaultLogger();
        getInstance().getLogger().log(Level.INFO, "Welcome to {0}!", ConfigurationManager.getInstance().getConfig().get("System.gameTitle"));

        // Make sure to load the native libraries for current OS first
        try {
            loadNativeLibs();
        } catch (Exception e) {
            getInstance().getLogger().log(Level.SEVERE, "Couldn't link static libraries. Sorry. " + e.toString(), e);
        }

        Blockmania blockmania = null;

        try {
            blockmania = getInstance();

            blockmania.initDisplay();
            blockmania.initControls();
            blockmania.initGame();
            blockmania.initGroovy();
        } catch (LWJGLException e) {
            getInstance().getLogger().log(Level.SEVERE, "Failed to start game. I'm so sorry: " + e.toString(), e);
            System.exit(0);
        }

        // START THE MAIN GAME LOOP
        blockmania.startGame();

        System.exit(0);
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
     * Init. a new instance of Blockmania.
     */
    private Blockmania() {
        _logger = Logger.getLogger("blockmania");
    }

    /**
     * Init. the Groovy manager.
     */
    public void initGroovy() {
        _groovyManager = new GroovyManager();
    }

    /**
     * Init. the display.
     *
     * @throws LWJGLException Thrown when the LWJGL fails
     */
    public void initDisplay() throws LWJGLException {
        if ((Boolean) ConfigurationManager.getInstance().getConfig().get("Graphics.fullscreen")) {
            Display.setDisplayMode(Display.getDesktopDisplayMode());
            Display.setFullscreen(true);
        } else {
            Display.setDisplayMode((DisplayMode) ConfigurationManager.getInstance().getConfig().get("Graphics.displayMode"));
        }

        Display.setTitle((String) ConfigurationManager.getInstance().getConfig().get("System.gameTitle"));
        Display.create((PixelFormat) ConfigurationManager.getInstance().getConfig().get("Graphics.pixelFormat"));
    }

    /**
     * Init. keyboard and mouse input.
     *
     * @throws LWJGLException Thrown when the LWJGL fails
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
     * Init. a new random world.
     */
    public void initWorld() {
        initWorld(null, null);
    }

    /**
     * Prepares a new world with a given name and seed value.
     *
     * @param title Title of the world
     * @param seed  Seed value used for the generators
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

        getInstance().getLogger().log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Init. a new world
        _activeWorldRenderer = new WorldRenderer(title, seed);
        _activeWorldRenderer.setPlayer(new Player(_activeWorldRenderer));

        // Create the first Portal if it doesn't exist yet
        _activeWorldRenderer.initPortal();
        _activeWorldRenderer.setViewingDistance(VIEWING_DISTANCES[_activeViewingDistance]);

        simulateWorld(4000);
    }

    private void simulateWorld(int duration) {
        long timeBefore = getTime();

        _statusScreen.setVisible(true);
        _hud.setVisible(false);

        float diff = 0;

        for (; diff < duration; ) {
            _statusScreen.updateStatus(String.format("Fast forwarding world... %.2f%%! :-)", (diff / duration) * 100f));

            renderUserInterface();
            updateUserInterface();

            getActiveWorldRenderer().standaloneGenerateChunks();

            Display.update();

            diff = getTime() - timeBefore;
        }

        // Reset the delta value
        _lastLoopTime = getTime();

        _statusScreen.setVisible(false);
        _hud.setVisible(true);
    }

    /**
     * Clean up before exiting the application.
     */
    private void destroy() {
        AL.destroy();
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    public void initGame() {
        _timerTicksPerSecond = Sys.getTimerResolution();

        /*
         * Init. management classes.
         */
        ShaderManager.getInstance();
        VertexBufferObjectManager.getInstance();
        FontManager.getInstance();
        BlockManager.getInstance();

        _hud = new UIHeadsUpDisplay();
        _hud.setVisible(true);

        _pauseMenu = new UIPauseMenu();
        _statusScreen = new UIStatusScreen();

        /*
         * Init. OpenGL
         */
        resizeViewport();
        resetOpenGLParameters();

        // Generate a world with a random seed value
        String worldSeed = (String) ConfigurationManager.getInstance().getConfig().get("World.defaultSeed");

        if (worldSeed.isEmpty())
            worldSeed = null;

        initWorld("World1", worldSeed);
        initGroovy();
    }

    public void resetOpenGLParameters() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glShadeModel(GL_FLAT);

        // Update the viewing distance
        double minDist = (VIEWING_DISTANCES[_activeViewingDistance] / 2) * 16.0f;
        glFogf(GL_FOG_START, (float) (minDist * 0.01));
        glFogf(GL_FOG_END, (float) minDist);
    }

    private void resizeViewport() {
        glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
    }

    /**
     * Starts the main game loop.
     */
    public void startGame() {
        getInstance().getLogger().log(Level.INFO, "Starting Blockmania...");
        _lastLoopTime = getTime();

        double nextGameTick = getTime();
        int loopCounter;


        // MAIN GAME LOOP
        while (_runGame && !Display.isCloseRequested()) {
            updateFPS();
            processKeyboardInput();
            processMouseInput();

            BlockmaniaProfiler.begin();

            loopCounter = 0;
            while (getTime() > nextGameTick && loopCounter < FRAME_SKIP_MAX_FRAMES) {
                update();
                nextGameTick += SKIP_TICKS;
                loopCounter++;
            }

            render();

            // Clear dirty flag and swap buffer
            Display.update();

            if (FPS_LIMIT > 0)
                Display.sync(FPS_LIMIT);

            BlockmaniaProfiler.end();
        }

        /*
         * Save the world and exit the application.
         */
        if (_saveWorldOnExit) {
            _activeWorldRenderer.dispose();
        }

        _threadPool.shutdown();

        try {
            _threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            getLogger().log(Level.SEVERE, e.toString(), e);
        }

        destroy();
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (_activeWorldRenderer != null)
            _activeWorldRenderer.render();

        renderUserInterface();
    }

    public void update() {
        if (!_pauseGame && !_hud.getDebugConsole().isVisible() && !_pauseMenu.isVisible()) {
            if (_activeWorldRenderer != null)
                _activeWorldRenderer.update();
            if (!Mouse.isGrabbed())
                Mouse.setGrabbed(true);
        } else {
            if (Mouse.isGrabbed())
                Mouse.setGrabbed(false);
        }

        if (_activeWorldRenderer != null) {
            if (_activeWorldRenderer.getPlayer().isDead()) {
                _statusScreen.setVisible(true);
                _statusScreen.updateStatus("Sorry. You've died. :-(");
            } else {
                _statusScreen.setVisible(false);
            }

        }

        updateUserInterface();
    }

    private void renderUserInterface() {
        _hud.render();
        _pauseMenu.render();
        _statusScreen.render();
    }

    private void updateUserInterface() {
        _hud.update();
        _pauseMenu.update();
        _statusScreen.update();
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

    public void togglePauseMenu() {
        _pauseMenu.setVisible(!_pauseMenu.isVisible());
    }

    public void exit(boolean saveWorld) {
        _saveWorldOnExit = saveWorld;
        _runGame = false;
    }

    public void exit() {
        exit(true);
    }

    /*
     * Process mouse input - nothing system-y, so just passing it to the Player class
     */
    private void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();

            if (!_pauseGame && !_hud.getDebugConsole().isVisible() && !_pauseMenu.isVisible())
                _activeWorldRenderer.getPlayer().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);

            _hud.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
            _pauseMenu.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
        }
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    private void processKeyboardInput() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (!Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                if (key == Keyboard.KEY_ESCAPE && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    togglePauseMenu();
                }

                if (key == Keyboard.KEY_F3 && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    ConfigurationManager.getInstance().getConfig().put("System.Debug.debug", !(Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.debug"));
                }

                if (key == Keyboard.KEY_F && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    toggleViewingDistance();
                }

                // Pass the pressed key on to the GUI
                _hud.processKeyboardInput(key);
                _pauseMenu.processKeyboardInput(key);
            }

            // Pass input to the current player
            if (!_pauseGame && !_hud.getDebugConsole().isVisible() && !_pauseMenu.isVisible())
                _activeWorldRenderer.getPlayer().processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
        }
    }

    /**
     * Updates the FPS display.
     */
    private void updateFPS() {
        // Measure the delta value and the frames per second
        long delta = getTime() - _lastLoopTime;

        _lastLoopTime = getTime();
        _lastFpsTime += delta;
        _fps++;

        // Update the FPS and calculate the average FPS
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

    private void initDefaultLogger() {
        File dirPath = new File("LOGS");

        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                return;
            }
        }

        addLogFileHandler("LOGS/blockmania.log", Level.INFO);
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

    public WorldRenderer getActiveWorldRenderer() {
        return _activeWorldRenderer;
    }

    public WorldProvider getActiveWorldProvider() {
        return _activeWorldRenderer.getWorldProvider();
    }

    /**
     * Returns the system time in ms.
     *
     * @return The system time in ms
     */
    public long getTime() {
        if (_timerTicksPerSecond == 0)
            return 0;

        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }

    public ThreadPoolExecutor getThreadPool() {
        return _threadPool;
    }

    public GroovyManager getGroovyManager() {
        return _groovyManager;
    }

    public void toggleViewingDistance() {
        _activeViewingDistance = (_activeViewingDistance + 1) % 4;
        _activeWorldRenderer.setViewingDistance(VIEWING_DISTANCES[_activeViewingDistance]);
    }
}

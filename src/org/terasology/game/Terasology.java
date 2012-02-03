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
package org.terasology.game;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.*;
import org.terasology.logic.world.WorldProvider;
import org.terasology.model.blocks.BlockManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.menus.*;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
 * The heart and soul of Terasology.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Terasology {

    /* VIEWING DISTANCE */
    private static final int[] VIEWING_DISTANCES = {(Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceNear"),
            (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceModerate"),
            (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceFar"),
            (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceUltra")};

    private int _activeViewingDistance = 0;

    /* THREADING */
    private final ThreadPoolExecutor _threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /* CONST */
    private static final int TICKS_PER_SECOND = 60;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;

    /* STATISTICS */
    private long _lastLoopTime, _lastFpsTime;
    private int _fps;
    private float _averageFps;
    private long _timerTicksPerSecond;
    private long _delta;

    private double _timeAccumulator = 0;

    /* GAME LOOP */
    private boolean _pauseGame = false, _runGame = true, _saveWorldOnExit = true;

    /* RENDERING */
    private WorldRenderer _activeWorldRenderer;

    /* GUI */
    private ArrayList<UIDisplayElement> _guiScreens = new ArrayList<UIDisplayElement>();
    private UIHeadsUpDisplay _hud;
    private UIMetrics _metrics;
    private UIPauseMenu _pauseMenu;
    private UIStatusScreen _statusScreen;
    private UIInventoryScreen _inventoryScreen;

    /* SINGLETON */
    private static Terasology _instance;

    /* LOGGING */
    private final Logger _logger;

    /* GROOVY */
    private GroovyManager _groovyManager;

    /**
     * Returns the static instance of Terasology.
     *
     * @return The instance
     */
    public static Terasology getInstance() {
        if (_instance == null)
            _instance = new Terasology();
        return _instance;
    }

    /**
     * Entry point of the application.
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        getInstance().initDefaultLogger();
        getInstance().getLogger().log(Level.INFO, "Welcome to Terasology | {0}!", ConfigurationManager.getInstance().getConfig().get("System.versionTag"));

        // Make sure to load the native libraries for current OS first
        try {
            loadNativeLibs();
        } catch (Exception e) {
            getInstance().getLogger().log(Level.SEVERE, "Couldn't link static libraries. Sorry. " + e.toString(), e);
        }

        Terasology terasology = null;

        try {
            terasology = getInstance();

            terasology.initDisplay();
            terasology.initControls();
            terasology.initGame();
            terasology.initGroovy();
        } catch (LWJGLException e) {
            getInstance().getLogger().log(Level.SEVERE, "Failed to start game. I'm so sorry: " + e.toString(), e);
            System.exit(0);
        }

        // START THE MAIN GAME LOOP
        terasology.startGame();

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
     * Init. a new instance of Terasology.
     */
    private Terasology() {
        _logger = Logger.getLogger("Terasology");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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

        Display.setResizable(true);
        Display.setTitle("Terasology" + " | " + ConfigurationManager.getInstance().getConfig().get("System.versionTag"));
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
        Mouse.create();
        Mouse.setGrabbed(true);
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
        _metrics.setVisible(false);

        float diff = 0;

        while (diff < duration) {
            _statusScreen.updateStatus(String.format("Fast forwarding world... %.2f%%! :-)", (diff / duration) * 100f));

            renderUserInterface();
            updateUserInterface();

            getActiveWorldRenderer().standaloneGenerateChunks();

            Display.update();

            diff = getTime() - timeBefore;
        }

        _statusScreen.setVisible(false);
        _hud.setVisible(true);
        _metrics.setVisible(true);
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
        _inventoryScreen = new UIInventoryScreen();
        _metrics = new UIMetrics();
        _metrics.setVisible(true);

        _guiScreens.add(_metrics);
        _guiScreens.add(_hud);
        _guiScreens.add(_pauseMenu);
        _guiScreens.add(_statusScreen);
        _guiScreens.add(_inventoryScreen);

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
        glFogf(GL_FOG_START, (float) (minDist * 0.5));
        glFogf(GL_FOG_END, (float) minDist);
    }

    private void resizeViewport() {
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    /**
     * Starts the main game loop.
     */
    public void startGame() {
        getInstance().getLogger().log(Level.INFO, "Starting Terasology...");

        PerformanceMonitor.setEnabled(true);
        PerformanceMonitor.startActivity("Other");

        // MAIN GAME LOOP
        while (_runGame && !Display.isCloseRequested()) {
            if (!Display.isActive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
                }
                PerformanceMonitor.startActivity("Process Display");
                Display.processMessages();
                PerformanceMonitor.endActivity();
                continue;
            }

            PerformanceMonitor.startActivity("Main Update");
            long startTime = getTime();
            while (_timeAccumulator >= SKIP_TICKS) {
                update();
                _timeAccumulator -= SKIP_TICKS;
            }
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Render");
            render();
            updateFps();
            Display.update();
            Display.sync(60);

            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Input");
            processKeyboardInput();
            processMouseInput();

            if (!screenHasFocus())
                getActiveWorldRenderer().getPlayer().updateInput();

            PerformanceMonitor.endActivity();
            PerformanceMonitor.endActivity();

            PerformanceMonitor.rollCycle();
            PerformanceMonitor.startActivity("Other");

            _timeAccumulator += getTime() - startTime;

            if (Display.wasResized())
                resizeViewport();
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

        PerformanceMonitor.startActivity("Render UI");
        renderUserInterface();
        PerformanceMonitor.endActivity();
    }

    public void update() {
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

        updateUserInterface();
    }

    private boolean screenHasFocus() {
        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay()) {
                return true;
            }
        }

        return false;
    }

    private boolean screenCanFocus(UIDisplayElement s) {
        boolean result = true;

        for (UIDisplayElement screen : _guiScreens) {
            if (screen.isVisible() && !screen.isOverlay() && screen != s)
                result = false;
        }

        return result;
    }

    private boolean shouldUpdateWorld() {
        return !_pauseGame && !_pauseMenu.isVisible();
    }

    private void renderUserInterface() {
        for (UIDisplayElement screen : _guiScreens) {
            screen.render();
        }
    }

    private void updateUserInterface() {
        for (UIDisplayElement screen : _guiScreens) {
            screen.update();
        }
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
        if (screenCanFocus(_pauseMenu))
            _pauseMenu.setVisible(!_pauseMenu.isVisible());
    }

    public void toggleViewingDistance() {
        _activeViewingDistance = (_activeViewingDistance + 1) % 4;
        _activeWorldRenderer.setViewingDistance(VIEWING_DISTANCES[_activeViewingDistance]);
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

            for (UIDisplayElement screen : _guiScreens) {
                if (screenCanFocus(screen)) {
                    screen.processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
                }
            }

            if (!screenHasFocus())
                _activeWorldRenderer.getPlayer().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
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

                if (key == Keyboard.KEY_I && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    toggleInventory();
                }

                if (key == Keyboard.KEY_F3 && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    ConfigurationManager.getInstance().getConfig().put("System.Debug.debug", !(Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.debug"));
                }

                if (key == Keyboard.KEY_F && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    toggleViewingDistance();
                }

                // Pass input to focused GUI element
                for (UIDisplayElement screen : _guiScreens) {
                    if (screenCanFocus(screen)) {
                        screen.processKeyboardInput(key);
                    }
                }
            }

            // Pass input to the current player
            if (!screenHasFocus())
                _activeWorldRenderer.getPlayer().processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
        }
    }

    /**
     * Updates the FPS display.
     */
    private void updateFps() {
        // Measure the delta value and the frames per second
        long now = getTime();
        _delta = now - _lastLoopTime;

        _lastLoopTime = now;
        _lastFpsTime += _delta;
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

        addLogFileHandler("LOGS/Terasology.log", Level.INFO);
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

    public void submitTask(final String name, final Runnable task)
    {
        _threadPool.execute(new Runnable() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                PerformanceMonitor.startThread(name);
                try
                {
                    task.run();
                }
                finally
                {
                    PerformanceMonitor.endThread(name);
                }
            }
        });
    }
    
    public int activeTasks()
    {
        return _threadPool.getActiveCount();
    }

    public GroovyManager getGroovyManager() {
        return _groovyManager;
    }

    public long getDelta() {
        return _delta;
    }
}

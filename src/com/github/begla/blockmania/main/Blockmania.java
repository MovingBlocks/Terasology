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

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.groovy.GroovyManager;
import com.github.begla.blockmania.gui.HUD;
import com.github.begla.blockmania.rendering.FontManager;
import com.github.begla.blockmania.rendering.RenderableScene;
import com.github.begla.blockmania.rendering.ShaderManager;
import com.github.begla.blockmania.rendering.VBOManager;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.World;
import com.github.begla.blockmania.world.WorldProvider;
import com.github.begla.blockmania.world.characters.MobManager;
import com.github.begla.blockmania.world.characters.Player;
import com.github.begla.blockmania.world.chunk.Chunk;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.newdawn.slick.SlickException;

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
public final class Blockmania extends RenderableScene {

    private final ThreadPoolExecutor _threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(32);
    /* ------ */
    private static final int FRAME_SKIP_MAX_FRAMES = 5;
    private static final int TICKS_PER_SECOND = 60;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    /* ------- */
    private long _lastLoopTime, _lastFpsTime;
    private double _averageFps;
    private int _fps;
    private boolean _pauseGame = false, _runGame = true, _saveWorldOnExit = true;
    /* ------- */
    private World _world;
    private HUD _hud;
    /* ------- */
    private long _timerTicksPerSecond;
    /* ------- */
    private static Blockmania _instance;
    /* ------- */
    private final Logger _logger = Logger.getLogger("blockmania");
    /* ------- */
    private final BlockmaniaConsole _console = new BlockmaniaConsole(this);

    /**
     * Groovy Manager handles all the Groovy-related stuff!
     */
    private GroovyManager _groovyManager;

    /**
     * Mob Manager to deal with non-player character
     */
    private MobManager _mobManager;

    // Singleton
    public static Blockmania getInstance() {
        if (_instance == null)
            _instance = new Blockmania();
        return _instance;
    }

    private Blockmania() {
        _hud = new HUD(this);
    }

    /**
     * Entry point of the application.
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        initDefaultLogger();
        getInstance().getLogger().log(Level.INFO, "Welcome to {0}!", ConfigurationManager.getInstance().getConfig().get("System.gameTitle"));

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
            getInstance().getLogger().log(Level.SEVERE, "Failed to start game. I'm sorry. " + e.toString(), e);
        } catch (SlickException e) {
            getInstance().getLogger().log(Level.SEVERE, "Failed to start game. I'm sorry. " + e.toString(), e);
        }

        // MAIN GAME LOOP
        if (blockmania != null)
            blockmania.startGame();

        System.exit(0);
    }

    private static void initDefaultLogger() {
        File dirPath = new File("logs");

        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                return;
            }
        }

        getInstance().addLogFileHandler("logs/blockmania.log", Level.INFO);
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

        getInstance().getLogger().log(Level.INFO, "Creating new World with seed \"{0}\"", seed);

        // Get rid of the old world
        if (_world != null) {
            _world.dispose();
            _world = null;
        }

        if (seed == null) {
            seed = random.randomCharacterString(16);
        } else if (seed.isEmpty()) {
            seed = random.randomCharacterString(16);
        }

        // Init. a new world
        _world = new World(title, seed);
        _world.setPlayer(new Player(_world));

        // Reset the delta value
        _lastLoopTime = getTime();
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

    public void initGame() throws SlickException {
        _timerTicksPerSecond = Sys.getTimerResolution();

        /*
         * Init. management classes.
         */
        ShaderManager.getInstance();
        VBOManager.getInstance();
        FontManager.getInstance();
        BlockManager.getInstance();

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

        _mobManager = new MobManager(); // I suppose this could/should be a getInstance...
        initGroovy();
    }

    public void resetOpenGLParameters() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        // Update the viewing distance
        double minDist = Math.min((Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceX") * Chunk.getChunkDimensionX(), (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceZ") * Chunk.getChunkDimensionZ()) / 2.0;
        glFogf(GL_FOG_START, (float) (minDist * 0.5));
        glFogf(GL_FOG_END, (float) minDist);

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        glShadeModel(GL11.GL_SMOOTH);
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

        /*
         * GAME LOOP.
         */
        while (_runGame && !Display.isCloseRequested()) {
            updateFPS();
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
            getLogger().log(Level.SEVERE, e.toString(), e);
        }

        destroy();
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (_world != null)
            _world.render();

        super.render();

        if (_hud != null)
            _hud.render();

        _mobManager.renderAll();
    }

    public void update() {
        if (_world != null)
            _world.update();

        super.update();

        if (_hud != null)
            _hud.update();

        _mobManager.updateAll();
    }

    public void pause() {
        Mouse.setGrabbed(false);
        _pauseGame = true;
    }

    public void unpause() {
        _pauseGame = false;
        Mouse.setGrabbed(true);
    }

    public void exit(boolean saveWorld) {
        _saveWorldOnExit = saveWorld;
        _runGame = false;
    }

    public void exit() {
        _saveWorldOnExit = true;
        _runGame = false;
    }

    /*
     * Process mouse input - nothing system-y, so just passing it to the Player class
     */
    private void processMouseInput() {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();
            _world.getPlayer().processMouseInput(button, Mouse.getEventButtonState(), wheelMoved);
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
                    togglePauseGame();
                }

                if (key == Keyboard.KEY_F3 && !Keyboard.isRepeatEvent() && Keyboard.getEventKeyState()) {
                    ConfigurationManager.getInstance().getConfig().put("System.Debug.debug", !(Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.debug"));
                }

                if (isGamePaused()) {
                    _console.processKeyboardInput(key);
                }
            }

            // Pass input to the current player
            if (!isGamePaused())
                _world.getPlayer().processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
        }
    }

    /**
     * Updates the FPS display.
     */
    private void updateFPS() {
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

    public void togglePauseGame() {
        _pauseGame = !_pauseGame;
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

    public WorldProvider getActiveWorldProvider() {
        return _world.getWorldProvider();
    }

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

    public BlockmaniaConsole getConsole() {
        return _console;
    }

    public MobManager getMobManager() {
        return _mobManager;
    }
}

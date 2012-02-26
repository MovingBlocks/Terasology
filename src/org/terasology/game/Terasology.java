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
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.game.modes.IGameMode;
import org.terasology.game.modes.ModeMainMenu;
import org.terasology.game.modes.ModePlayGame;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.*;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.shapes.BlockShapeManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.world.WorldRenderer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
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
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 * @todo To create the function that will return the number of generated worlds
 */
public final class Terasology {
    private static Terasology _instance = new Terasology();

    private final Logger _logger = Logger.getLogger("Terasology");
    private final GroovyManager _groovyManager = new GroovyManager();
    private final ThreadPoolExecutor _threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /* STATISTICS */
    private long _lastLoopTime, _lastFpsTime;
    private int _fps;
    private float _averageFps;
    private long _timerTicksPerSecond;
    private long _delta;

    /* GAME LOOP */
    private boolean _runGame = true, _saveWorldOnExit = true;

    /* GAME MODES */
    public enum GameMode {
        undefined, mainMenu, runGame
    }
    static GameMode _state = GameMode.mainMenu;
    private static Map<GameMode, IGameMode> _gameModes = Collections.synchronizedMap(new EnumMap<GameMode, IGameMode>(GameMode.class));

    public static Terasology getInstance() {
        return _instance;
    }

    private Terasology() {
    }

    public void init(){
        _logger.log(Level.INFO, "Initializing Terasology...");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        initLogger();
        initNativeLibs();
        initOpenAL();
        initDisplay();
        initOpenGL();
        initControls();
        initManagers();
        //refactor to class -> timer / stats
        _timerTicksPerSecond = Sys.getTimerResolution();
    }

    private void initNativeLibs() {
        if (System.getProperty("os.name").equals("Mac OS X"))
            addLibraryPath("natives/macosx");
        else if (System.getProperty("os.name").equals("Linux"))
            addLibraryPath("natives/linux");
        else {
            addLibraryPath("natives/windows");

            if (System.getProperty("os.arch").equals("amd64") || System.getProperty("os.arch").equals("x86_64"))
                System.loadLibrary("OpenAL64");
            else
                System.loadLibrary("OpenAL32");
        }
    }

    private void addLibraryPath(String s){
        try {
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
        } catch(Exception e){
            _logger.log(Level.SEVERE, "Couldn't link static libraries. " + e.toString(), e);
            //brutal...
            System.exit(1);
        }
    }

    private void initOpenAL() {
        AudioManager.getInstance();
    }

    private void initDisplay() {
        try {
            if ((Boolean) SettingsManager.getInstance().getUserSetting("Game.Graphics.fullscreen")) {
                Display.setDisplayMode(Display.getDesktopDisplayMode());
                Display.setFullscreen(true);
            } else {
                Display.setDisplayMode((DisplayMode) SettingsManager.getInstance().getUserSetting("Game.Graphics.displayMode"));
                Display.setResizable(true);
            }

            Display.setTitle("Terasology" + " | " + "Pre Alpha");
            Display.create((PixelFormat) SettingsManager.getInstance().getUserSetting("Game.Graphics.pixelFormat"));
        } catch (LWJGLException e){
            _logger.log(Level.SEVERE, "Can not initialize graphics device.", e);
            System.exit(1);
        }
    }

    private void initOpenGL() {
        checkOpenGL();
        resizeViewport();
        resetOpenGLParameters();
    }

    private void checkOpenGL(){
        boolean canRunGame = GLContext.getCapabilities().OpenGL20
                & GLContext.getCapabilities().OpenGL11
                & GLContext.getCapabilities().OpenGL12
                & GLContext.getCapabilities().OpenGL14
                & GLContext.getCapabilities().OpenGL15;

        if (!canRunGame) {
            _logger.log(Level.SEVERE, "Your GPU driver is not supporting the mandatory versions of OpenGL. Considered updating your GPU drivers?");
            System.exit(1);
        }

    }

    private void resizeViewport() {
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void resetOpenGLParameters() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    private void initControls() {
        try {
            Keyboard.create();
            Keyboard.enableRepeatEvents(true);
            Mouse.create();
            Mouse.setGrabbed(true);
        } catch (LWJGLException e) {
            _logger.log(Level.SEVERE, "Could not initialize controls.", e);
            System.exit(1);
        }
    }

    private void initManagers() {
        ShaderManager.getInstance();
        VertexBufferObjectManager.getInstance();
        FontManager.getInstance();
        BlockShapeManager.getInstance();
        BlockManager.getInstance();
    }

    public void run(){
        _logger.log(Level.INFO, "Running Terasology...");
        PerformanceMonitor.startActivity("Other");
        IGameMode mode;
        // MAIN GAME LOOP
        while (_runGame && !Display.isCloseRequested()) {
            // Only process rendering and updating once a second
            if (!Display.isActive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
                }
                PerformanceMonitor.startActivity("Process Display");
                Display.processMessages();
                PerformanceMonitor.endActivity();
            }

            mode = getGameMode();

            if (mode == null) {
                _runGame = false;
                break;
            }

            PerformanceMonitor.startActivity("Main Update");
            long startTime = getTimeInMs();
            mode.update();
            PerformanceMonitor.endActivity();
            PerformanceMonitor.startActivity("Render");
            mode.render();
            updateFps();
            Display.update();
            Display.sync(60);
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Input");
            mode.processKeyboardInput();
            mode.processMouseInput();
            mode.updatePlayerInput();
            PerformanceMonitor.endActivity();

            PerformanceMonitor.rollCycle();
            PerformanceMonitor.startActivity("Other");
            mode.updateTimeAccumulator(getTimeInMs(), startTime);

            if (Display.wasResized())
                resizeViewport();
        }
    }

    public void shutdown(){
        _logger.log(Level.INFO, "Shutting down Terasology...");
        saveWorld();
        terminateThreads();
        destroy();
    }

    private void saveWorld() {
        //UGLY! why does dispose save the world...
        if (_saveWorldOnExit) {
            if (getActiveWorldRenderer() != null)
                getGameMode().getActiveWorldRenderer().dispose();
        }
    }

    private void terminateThreads() {
        _threadPool.shutdown();
        try {
            _threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    private void destroy() {
        AL.destroy();
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }


    public IGameMode getGameMode() {
        IGameMode mode = _gameModes.get(_state);

        if (mode != null) {
            return mode;
        }

        switch (_state) {
            case runGame:
                mode = new ModePlayGame();
                break;

            case mainMenu:
                mode = new ModeMainMenu();
                break;

            case undefined:
                getLogger().log(Level.SEVERE, "Undefined game state - unable to run");
                return null;
        }

        _gameModes.put(_state, mode);

        mode.init();

        return mode;
    }

    public void setGameMode(GameMode state) {
        _state = state;
    }

    public void render() {
        getGameMode().render();
    }

    public void update() {
        getGameMode().update();
    }

    public void exit(boolean saveWorld) {
        _saveWorldOnExit = saveWorld;
        _runGame = false;
    }

    public void exit() {
        exit(true);
    }

    private void updateFps() {
        // Measure the delta value and the frames per second
        long now = getTimeInMs();
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

    private void initLogger() {
        File dirPath = new File("logs");

        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                return;
            }
        }

        addLogFileHandler("logs/Terasology.log", Level.INFO);
    }

    public String getWorldSavePath(String worldTitle) {
        String path = String.format("SAVED_WORLDS/%s", worldTitle);
        // Try to detect if we're getting a screwy save path (usually/always the case with an applet)
        File f = new File(path);
        //System.out.println("Suggested absolute save path is: " + f.getAbsolutePath());
        if (!f.getAbsolutePath().contains("Terasology")) {
            f = new File(System.getProperty("java.io.tmpdir"), path);
            //System.out.println("Absolute TEMP save path is: " + f.getAbsolutePath());
            return f.getAbsolutePath();
        }
        return path;
    }

    public Logger getLogger() {
        return _logger;
    }

    public double getAverageFps() {
        return _averageFps;
    }

    public WorldRenderer getActiveWorldRenderer() {
        return getGameMode().getActiveWorldRenderer();
    }

    public IWorldProvider getActiveWorldProvider() {
        return getGameMode().getActiveWorldRenderer().getWorldProvider();
    }

    public Player getActivePlayer() {
        return getGameMode().getActiveWorldRenderer().getPlayer();
    }

    public long getTimeInMs() {
        if (_timerTicksPerSecond == 0)
            return 0;

        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }

    public void submitTask(final String name, final Runnable task) {
        _threadPool.execute(new Runnable() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                PerformanceMonitor.startThread(name);
                try {
                    task.run();
                } finally {
                    PerformanceMonitor.endThread(name);
                }
            }
        });
    }

    public int activeTasks() {
        return _threadPool.getActiveCount();
    }

    public GroovyManager getGroovyManager() {
        return _groovyManager;
    }

    public static void main(String[] args) {
        _instance.init();
        _instance.run();
        _instance.shutdown();
        System.exit(0);
    }
}

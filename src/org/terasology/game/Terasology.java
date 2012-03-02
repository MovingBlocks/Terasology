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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.terasology.game.modes.IGameState;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.game.modes.StateSinglePlayer;
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
 * <p/>
 * TODO: Create a function returns the number of generated worlds
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 */
public final class Terasology {
    private static Terasology _instance = new Terasology();

    private final Logger _logger = Logger.getLogger("Terasology");
    private final GroovyManager _groovyManager = new GroovyManager();
    private final ThreadPoolExecutor _threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private Timer _timer;

    /* GAME LOOP */
    private boolean _runGame = true;

    /* GAME MODES */
    public enum GAME_STATE {
        UNDEFINED, MAIN_MENU, SINGLE_PLAYER
    }

    static GAME_STATE _state = GAME_STATE.MAIN_MENU;
    private static Map<GAME_STATE, IGameState> _gameStates = Collections.synchronizedMap(new EnumMap<GAME_STATE, IGameState>(GAME_STATE.class));

    public static Terasology getInstance() {
        return _instance;
    }

    private Terasology() {
    }

    public void init() {
        _logger.log(Level.INFO, "Initializing Terasology...");

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        initLogger();
        initNativeLibs();
        initOpenAL();
        initDisplay();
        initOpenGL();
        initControls();
        initManagers();
        initTimer(); // Dependant on LWJGL
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

    private void initNativeLibs() {
        if (System.getProperty("os.name").equals("Mac OS X"))
            addLibraryPath("natives/macosx");
        else if (System.getProperty("os.name").equals("Linux"))
            addLibraryPath("natives/linux");
        else {
            addLibraryPath("natives/windows");

            if (System.getProperty("os.arch").contains("64"))
                System.loadLibrary("OpenAL64");
            else
                System.loadLibrary("OpenAL32");
        }
    }

    private void addLibraryPath(String s) {
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
        } catch (Exception e) {
            _logger.log(Level.SEVERE, "Couldn't link static libraries. " + e.toString(), e);
            exit();
        }
    }

    private void initOpenAL() {
        AudioManager.getInstance();
    }

    private void initDisplay() {
        try {
            if (Config.getInstance().isFullscreen()) {
                Display.setDisplayMode(Display.getDesktopDisplayMode());
                Display.setFullscreen(true);
            } else {
                Display.setDisplayMode(Config.getInstance().getDisplayMode());
                Display.setResizable(true);
            }

            Display.setTitle("Terasology" + " | " + "Pre Alpha");
            Display.create(Config.getInstance().getPixelFormat());
        } catch (LWJGLException e) {
            _logger.log(Level.SEVERE, "Can not initialize graphics device.", e);
            exit();
        }
    }

    private void initOpenGL() {
        checkOpenGL();
        resizeViewport();
        initOpenGLParams();
    }

    private void checkOpenGL() {
        boolean canRunGame = GLContext.getCapabilities().OpenGL20
                & GLContext.getCapabilities().OpenGL11
                & GLContext.getCapabilities().OpenGL12
                & GLContext.getCapabilities().OpenGL14
                & GLContext.getCapabilities().OpenGL15;

        if (!canRunGame) {
            _logger.log(Level.SEVERE, "Your GPU driver is not supporting the mandatory versions of OpenGL. Considered updating your GPU drivers?");
            exit();
        }

    }

    private void resizeViewport() {
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void initOpenGLParams() {
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
            exit();
        }
    }

    private void initManagers() {
        ShaderManager.getInstance();
        VertexBufferObjectManager.getInstance();
        FontManager.getInstance();
        BlockShapeManager.getInstance();
        BlockManager.getInstance();
    }

    private void initTimer() {
        _timer = new Timer();
    }

    public void run() {
        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        IGameState state = null;
        while (_runGame && !Display.isCloseRequested()) {
            _timer.tick();

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

            IGameState prevState = state;
            state = getCurrentGameState();

            if (state == null) {
                exit();
                break;
            }

            if (state != prevState) {
                if (prevState != null)
                    prevState.deactivate();
                state.activate();
            }

            PerformanceMonitor.startActivity("Main Update");
            state.update(_timer.getDelta());
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Render");
            state.render();
            Display.update();
            Display.sync(60);
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Input");
            state.processKeyboardInput();
            state.processMouseInput();
            PerformanceMonitor.endActivity();

            PerformanceMonitor.rollCycle();
            PerformanceMonitor.startActivity("Other");

            if (Display.wasResized())
                resizeViewport();
        }
    }

    public void shutdown() {
        _logger.log(Level.INFO, "Shutting down Terasology...");
        getCurrentGameState().dispose();
        terminateThreads();
        destroy();
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


    public void exit() {
        _runGame = false;
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

    public String getWorldSavePath(String worldTitle) {
        String path = String.format("SAVED_WORLDS/%s", worldTitle);
        //TODO: Maybe use Helper.fixSavePath instead? Need to fix up save paths for manifest files etc sometime better anyway
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

    public IGameState getGameState(GAME_STATE s) {
        IGameState state = _gameStates.get(s);

        if (state != null) {
            return state;
        }

        switch (s) {
            case SINGLE_PLAYER:
                state = new StateSinglePlayer();
                break;

            case MAIN_MENU:
                state = new StateMainMenu();
                break;

            case UNDEFINED:
                getLogger().log(Level.SEVERE, "Undefined game state. Can not run!");
                return null;
        }

        state.init();

        _gameStates.put(_state, state);
        return state;
    }

    public IGameState getCurrentGameState() {
        return getGameState(_state);
    }

    public void setGameState(GAME_STATE state) {
        _state = state;
    }

    public Logger getLogger() {
        return _logger;
    }

    public double getAverageFps() {
        return _timer.getFps();
    }

    public WorldRenderer getActiveWorldRenderer() {
        StateSinglePlayer singlePlayer = (StateSinglePlayer) getGameState(GAME_STATE.SINGLE_PLAYER);
        return singlePlayer.getWorldRenderer();
    }

    public IWorldProvider getActiveWorldProvider() {
        if (getActiveWorldRenderer() != null)
            return getActiveWorldRenderer().getWorldProvider();
        return null;
    }

    public Player getActivePlayer() {
        if (getActiveWorldRenderer() != null)
            return getActiveWorldRenderer().getPlayer();
        return null;
    }

    public long getTimeInMs() {
        if (_timer == null) {
            initTimer();
        }

        return _timer.getTimeInMs();
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

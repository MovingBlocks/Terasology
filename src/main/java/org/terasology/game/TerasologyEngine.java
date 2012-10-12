/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.Lists;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.reflections.Reflections;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.game.modes.GameState;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.FontManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.InputConfig;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.VertexBufferObjectManager;
import org.terasology.logic.mod.ModManager;
import org.terasology.logic.mod.ModSecurityManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.version.TerasologyVersion;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * @author Immortius
 */
public class TerasologyEngine implements GameEngine {

    private Logger logger = Logger.getLogger(getClass().getName());

    private Deque<GameState> stateStack = new ArrayDeque<GameState>();
    private boolean initialised;
    private boolean running;
    private boolean disposed;
    private List<StateChangeFunction> pendingStateChanges = Lists.newArrayList();

    private Timer timer;
    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public TerasologyEngine() {
    }

    @Override
    public void init() {
        if (initialised) {
            return;
        }
        initLogger();
        logger.log(Level.INFO, "Initializing Terasology...");
        logger.log(Level.INFO, TerasologyVersion.getInstance().toString());

        initNativeLibs();
        initDisplay();
        initOpenGL();
        initOpenAL();
        initControls();
        initManagers();
        initTimer(); // Dependant on LWJGL
        initSecurity();
        initialised = true;
    }

    private void initSecurity() {
        // TODO: More work on security
        ModSecurityManager modSecurityManager = new ModSecurityManager();
        //System.setSecurityManager(modSecurityManager);
        modSecurityManager.addModAvailableClass(GUIManager.class);
        // TODO: Add in mod available classes

    }

    private void initLogger() {
        if (LWJGLUtil.DEBUG) {
            System.setOut(new PrintStream(System.out) {
                @Override
                public void print(final String message) {
                    Logger.getLogger("").info(message);
                }
            });
            System.setErr(new PrintStream(System.err) {
                @Override
                public void print(final String message) {
                    Logger.getLogger("").severe(message);
                }
            });
        }
        File dirPath = PathManager.getInstance().getLogPath();

        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                return;
            }
        }

        try {
            FileHandler fh = new FileHandler(new File(dirPath, "Terasology.log").getAbsolutePath(), false);
            fh.setLevel(Level.INFO);
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fh);
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.toString(), ex);
        }
    }

    @Override
    public void run(GameState initialState) {
        if (!initialised) {
            init();
        }
        changeState(initialState);
        running = true;
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        CoreRegistry.put(GameEngine.class, this);

        mainLoop();

        cleanup();
    }

    @Override
    public void shutdown() {
        running = false;
    }

    @Override
    public void dispose() {
        if (!running) {
            disposed = true;
            initialised = false;
            AudioManager.getInstance().destroy();
            Mouse.destroy();
            Keyboard.destroy();
            Display.destroy();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void changeState(GameState newState) {
        if (running) {
            pendingStateChanges.add(new ChangeState(newState));
        } else {
            doPurgeStates();
            doPushState(newState);
        }
    }

    @Override
    public void pushState(GameState newState) {
        if (running) {
            pendingStateChanges.add(new PushState(newState));
        } else {
            doPushState(newState);
        }
    }

    @Override
    public void popState() {
        if (running) {
            pendingStateChanges.add(new PopState());
        } else {
            doPopState();
        }
    }

    @Override
    public void submitTask(final String name, final Runnable task) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                PerformanceMonitor.startThread(name);
                try {
                    task.run();
                } catch (RejectedExecutionException e) {
                    logger.log(Level.SEVERE, "Thread submitted after shutdown requested: " + name);
                } finally {
                    PerformanceMonitor.endThread(name);
                }
            }
        });
    }

    @Override
    public int getActiveTaskCount() {
        return threadPool.getActiveCount();
    }

    private void initNativeLibs() {
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_MACOSX:
                addLibraryPath(new File(PathManager.getInstance().getDataPath(), "natives/macosx"));
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                addLibraryPath(new File(PathManager.getInstance().getDataPath(), "natives/linux"));
                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("openal64");
                } else {
                    System.loadLibrary("openal");
                }
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                addLibraryPath(new File(PathManager.getInstance().getDataPath(), "natives/windows"));

                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("OpenAL64");
                } else {
                    System.loadLibrary("OpenAL32");
                }
                break;
            default:
                logger.log(Level.SEVERE, "Unsupported operating system: " + LWJGLUtil.getPlatformName());
                System.exit(1);
        }
    }

    private void addLibraryPath(File libPath) {
        try {
            String envPath = System.getProperty("java.library.path");
            if (envPath == null || envPath.isEmpty()) {
                System.setProperty("java.library.path", libPath.getAbsolutePath());
            } else {
                System.setProperty("java.library.path", envPath + File.pathSeparator + libPath.getAbsolutePath());
            }

            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            List<String> paths = new ArrayList<String>(Arrays.asList((String[]) usrPathsField.get(null)));

            if (paths.contains(libPath)) {
                return;
            }

            paths.add(0, libPath.getAbsolutePath()); // Add to beginning, to override system libraries

            usrPathsField.set(null, paths.toArray(new String[paths.size()]));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Couldn't link static libraries. " + e.toString(), e);
            System.exit(1);
        }
    }

    private void initOpenAL() {
        // TODO: Put in registry
        AudioManager.getInstance().initialize();
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
            logger.log(Level.SEVERE, "Can not initialize graphics device.", e);
            System.exit(1);
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
            logger.log(Level.SEVERE, "Your GPU driver is not supporting the mandatory versions of OpenGL. Considered updating your GPU drivers?");
            System.exit(1);
        }

    }

    private void resizeViewport() {
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void initOpenGLParams() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_NORMALIZE);
        glDepthFunc(GL_LEQUAL);
    }

    private void initControls() {
        try {
            Keyboard.create();
            Keyboard.enableRepeatEvents(true);
            Mouse.create();
            Mouse.setGrabbed(true);
        } catch (LWJGLException e) {
            logger.log(Level.SEVERE, "Could not initialize controls.", e);
            System.exit(1);
        }
    }

    private void initManagers() {
        CoreRegistry.put(CollisionGroupManager.class, new CollisionGroupManager());
        CoreRegistry.put(ModManager.class, new ModManager());

        AssetType.registerAssetTypes();
        AssetManager.getInstance().addAssetSource(new ClasspathSource(ModManager.ENGINE_PACKAGE, getClass().getProtectionDomain().getCodeSource(), ModManager.ASSETS_SUBDIRECTORY));

        ShaderManager.getInstance();
        VertexBufferObjectManager.getInstance();
        FontManager.getInstance();
    }

    private void initTimer() {
        timer = new Timer();
        CoreRegistry.put(Timer.class, timer);
    }

    private void cleanup() {
        logger.log(Level.INFO, "Shutting down Terasology...");
        Config.getInstance().saveConfig(new File(PathManager.getInstance().getWorldPath(), "last.cfg"));
        InputConfig.getInstance().saveConfig(new File(PathManager.getInstance().getWorldPath(), "lastinput.cfg"));
        doPurgeStates();
        terminateThreads();
    }

    private void terminateThreads() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }

    private void mainLoop() {
        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        GameState state = null;
        while (running && !Display.isCloseRequested()) {

            // Only process rendering and updating once a second
            // TODO: Add debug config setting to run even if display inactive
            if (!Display.isActive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, e.toString(), e);
                }

                Display.processMessages();
                continue;
            }

            processStateChanges();
            state = stateStack.peek();

            if (state == null) {
                shutdown();
                break;
            }

            timer.tick();

            PerformanceMonitor.startActivity("Main Update");
            state.update(timer.getDelta());
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Render");
            state.render();
            Display.update();
            Display.sync(60);
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Input");
            state.handleInput(timer.getDelta());
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Audio");
            AudioManager.getInstance().update();
            PerformanceMonitor.endActivity();

            PerformanceMonitor.rollCycle();
            PerformanceMonitor.startActivity("Other");

            if (Display.wasResized()) {
                resizeViewport();
            }
        }
        PerformanceMonitor.endActivity();
        running = false;
    }

    private void processStateChanges() {
        for (StateChangeFunction func : pendingStateChanges) {
            func.enact();
        }
        pendingStateChanges.clear();
    }

    private void doPurgeStates() {
        while (!stateStack.isEmpty()) {
            doPopState();
        }
    }

    private void doPopState() {
        GameState oldState = stateStack.pop();
        oldState.deactivate();
        oldState.dispose();
        if (!stateStack.isEmpty()) {
            stateStack.peek().activate();
        }
    }

    private void doPushState(GameState newState) {
        if (!stateStack.isEmpty()) {
            stateStack.peek().deactivate();
        }
        stateStack.push(newState);
        newState.init(this);
        newState.activate();
    }

    private interface StateChangeFunction {
        void enact();
    }

    private class ChangeState implements StateChangeFunction {
        public GameState newState;

        public ChangeState(GameState newState) {
            this.newState = newState;
        }

        @Override
        public void enact() {
            doPurgeStates();
            doPushState(newState);
        }
    }

    private class PushState implements StateChangeFunction {
        public GameState newState;

        public PushState(GameState newState) {
            this.newState = newState;
        }

        @Override
        public void enact() {
            doPushState(newState);
        }
    }

    private class PopState implements StateChangeFunction {

        public PopState() {
        }

        @Override
        public void enact() {
            doPopState();
        }
    }
}

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

import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.config.InputConfig;
import org.terasology.game.modes.GameState;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.VertexBufferObjectManager;
import org.terasology.logic.mod.ModManager;
import org.terasology.logic.mod.ModSecurityManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.version.TerasologyVersion;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

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

    private static final Logger logger = LoggerFactory.getLogger(TerasologyEngine.class);

    private GameState currentState;
    private boolean initialised;
    private boolean running;
    private boolean disposed;
    private GameState pendingState;

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

        logger.info("Initializing Terasology...");
        logger.info(TerasologyVersion.getInstance().toString());

        initConfig();

        initNativeLibs();
        initDisplay();
        initOpenGL();
        initOpenAL();
        initControls();
        initManagers();
        updateInputConfig();
        initTimer(); // Dependent on LWJGL
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

    private void initConfig() {
        if (org.terasology.config.Config.getConfigFile().exists()) {
            org.terasology.config.Config config;
            try {
                config = org.terasology.config.Config.load(org.terasology.config.Config.getConfigFile());
                config.getDefaultModConfig().addMod("core");

            } catch (IOException e) {
                logger.error("Failed to load config", e);
                config = new org.terasology.config.Config();
                config.getDefaultModConfig().addMod("core");
            }
            CoreRegistry.put(org.terasology.config.Config.class, config);
        } else {
            org.terasology.config.Config config = new org.terasology.config.Config();
            config.getDefaultModConfig().addMod("core");
            CoreRegistry.put(org.terasology.config.Config.class, config);
        }
    }

    private void updateInputConfig() {
        org.terasology.config.Config config = CoreRegistry.get(org.terasology.config.Config.class);
        InputConfig.updateForChangedMods(config.getInputConfig());
        config.save();
    }

    private void initLogger() {
        if (LWJGLUtil.DEBUG) {
            System.setOut(new PrintStream(System.out) {
                private Logger logger = LoggerFactory.getLogger("org.lwjgl");

                @Override
                public void print(final String message) {
                    logger.info(message);
                }
            });
            System.setErr(new PrintStream(System.err) {
                private Logger logger = LoggerFactory.getLogger("org.lwjgl");

                @Override
                public void print(final String message) {
                    logger.error(message);
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
            java.util.logging.Logger.getLogger("").getHandlers()[0].setLevel(Level.FINE);
            java.util.logging.Logger.getLogger("org.terasology").setLevel(Level.INFO);
            java.util.logging.Logger.getLogger("").getHandlers()[0].setFormatter(new Formatter() {
                DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    String thrownMessage = "";
                    if (record.getThrown() != null) {
                        try {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            thrownMessage = sw.toString();
                        } catch (Exception ex) {
                        }
                    }
                    return String.format("[%s] (%s)\t%s:%s() - %s (Thread: %d)\n%s", record.getLevel().getLocalizedName(), dateFormat.format(new Date(record.getMillis())), record.getLoggerName(), record.getSourceMethodName(), formatMessage(record), record.getThreadID(), thrownMessage);
                }
            });
            FileHandler fh = new FileHandler(new File(dirPath, "Terasology.log").getAbsolutePath(), false);
            fh.setLevel(Level.INFO);
            fh.setFormatter(new Formatter() {
                DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    String thrownMessage = "";
                    if (record.getThrown() != null) {
                        try {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            thrownMessage = sw.toString();
                        } catch (Exception ex) {
                        }
                    }
                    return String.format("[%s] (%s)\t%s - %s\n%s", record.getLevel().getLocalizedName(), dateFormat.format(new Date(record.getMillis())), record.getLoggerName(), record.getSourceMethodName(), formatMessage(record), thrownMessage);
                }
            });
            java.util.logging.Logger.getLogger("").addHandler(fh);
        } catch (IOException ex) {
            logger.error("Failed to set up logging to file", ex);
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
            pendingState = newState;
        } else {
            switchState(newState);
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
                    logger.error("Thread submitted after shutdown requested: {}", name);
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
                logger.error("Unsupported operating system: {}", LWJGLUtil.getPlatformName());
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

            if (paths.contains(libPath.getAbsolutePath())) {
                return;
            }

            paths.add(0, libPath.getAbsolutePath()); // Add to beginning, to override system libraries

            usrPathsField.set(null, paths.toArray(new String[paths.size()]));
        } catch (Exception e) {
            logger.error("Couldn't link static libraries. ", e);
            System.exit(1);
        }
    }

    private void initOpenAL() {
        // TODO: Put in registry
        AudioManager.getInstance().initialize();
    }

    private void initDisplay() {
        try {
            setDisplayMode();
            Display.setTitle("Terasology" + " | " + "Pre Alpha");
            Display.create(Config.getInstance().getPixelFormat());
        } catch (LWJGLException e) {
            logger.error("Can not initialize graphics device.", e);
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
            final String message = "Your GPU driver is not supporting the mandatory versions of OpenGL. Considered updating your GPU drivers? Exiting...";

            logger.error(message);
            JOptionPane.showMessageDialog(null, message, "Mandatory OpenGL version(s) not supported", JOptionPane.ERROR_MESSAGE);

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
            logger.error("Could not initialize controls.", e);
            System.exit(1);
        }
    }

    private void initManagers() {
        CoreRegistry.put(CollisionGroupManager.class, new CollisionGroupManager());
        CoreRegistry.put(ModManager.class, new ModManager());
        CoreRegistry.put(ComponentSystemManager.class, new ComponentSystemManager());

        AssetType.registerAssetTypes();
        AssetManager.getInstance().addAssetSource(new ClasspathSource(ModManager.ENGINE_PACKAGE, getClass().getProtectionDomain().getCodeSource(), ModManager.ASSETS_SUBDIRECTORY, ModManager.OVERRIDES_SUBDIRECTORY));

        ShaderManager.getInstance();
        VertexBufferObjectManager.getInstance();
    }

    private void initTimer() {
        timer = new Timer();
        CoreRegistry.put(Timer.class, timer);
    }

    private void cleanup() {
        logger.info("Shutting down Terasology...");
        Config.getInstance().saveConfig(new File(PathManager.getInstance().getWorldPath(), "last.cfg"));
        if (currentState != null) {
            currentState.dispose();
            currentState = null;
        }
        terminateThreads();
    }

    private void terminateThreads() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.error("Error terminating thread pool.", e);
        }
    }

    private void mainLoop() {
        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        while (running && !Display.isCloseRequested()) {

            // Only process rendering and updating once a second
            // TODO: Add debug config setting to run even if display inactive
            if (!Display.isActive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("Display inactivity sleep interrupted", e);
                }

                Display.processMessages();
                continue;
            }

            processStateChanges();

            if (currentState == null) {
                shutdown();
                break;
            }

            timer.tick();

            PerformanceMonitor.startActivity("Main Update");
            currentState.update(timer.getDelta());
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Render");
            currentState.render();
            Display.update();
            Display.sync(60);
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Input");
            currentState.handleInput(timer.getDelta());
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
        if (pendingState != null) {
            switchState(pendingState);
            pendingState = null;
        }
    }

    private void switchState(GameState newState) {
        if (currentState != null) {
            currentState.dispose();
        }
        currentState = newState;
        newState.init(this);
    }

    private void setDisplayMode() {
        try {
            if (Config.getInstance().isFullscreen()) {
                Display.setDisplayMode(Display.getDesktopDisplayMode());
                Display.setFullscreen(true);
            } else {
                Display.setDisplayMode(Config.getInstance().getDisplayMode());
                Display.setResizable(true);
            }
        } catch (LWJGLException e) {
            logger.error("Can not initialize graphics device.", e);
            System.exit(1);
        }
    }

    public void setFullscreen(Boolean state) {
        if (Config.getInstance().isFullscreen() != state) {
            Config.getInstance().setFullscreen(state);
            setDisplayMode();
            resizeViewport();
            CoreRegistry.get(GUIManager.class).update(true);
        }
    }
}

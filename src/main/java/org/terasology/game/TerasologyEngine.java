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
import org.lwjgl.opengl.GLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.audio.AudioManager;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.audio.openAL.OpenALManager;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.game.modes.GameState;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.VertexBufferObjectManager;
import org.terasology.logic.mod.ModManager;
import org.terasology.logic.mod.ModSecurityManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.version.TerasologyGameVersionInfo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private AudioManager audioManager;
    private Config config;

    private Timer timer;
    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private Canvas customViewPort = null;
    private static boolean editorInFocus = false;
    private static boolean editorAttached = false;

    public TerasologyEngine() {
    }

    @Override
    public void init() {
        if (initialised) {
            return;
        }
        initLogger();

        logger.info("Initializing Terasology...");
        logger.info(TerasologyGameVersionInfo.getInstance().toString());

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
        if (Config.getConfigFile().exists()) {
            try {
                config = Config.load(Config.getConfigFile());
                config.getDefaultModSelection().addMod("core");
            } catch (IOException e) {
                logger.error("Failed to load config", e);
                config = new Config();
                config.getDefaultModSelection().addMod("core");
            }
            CoreRegistry.put(Config.class, config);
        } else {
            config = new Config();
            config.getDefaultModSelection().addMod("core");
            config.save();
            CoreRegistry.put(Config.class, config);
        }
    }

    private void updateInputConfig() {
        Config config = CoreRegistry.get(Config.class);
        BindsConfig.updateForChangedMods(config.getInputConfig().getBinds());
        config.save();
    }

    private void initLogger() {
        if (LWJGLUtil.DEBUG) {
            // Pipes System.out and err to log, because that's where lwjgl writes it to.
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
            Mouse.destroy();
            Keyboard.destroy();
            Display.destroy();
            audioManager.dispose();
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
        if (config.getAudio().isDisableSound()) {
            audioManager = new NullAudioManager();
        } else {
            audioManager = new OpenALManager(config.getAudio());
        }
        CoreRegistry.put(AudioManager.class, audioManager);
    }

    private void initDisplay() {
        try {
            setDisplayMode();
            Display.setParent(customViewPort);
            Display.setTitle("Terasology" + " | " + "Pre Alpha");
            Display.create(config.getRendering().getPixelFormat());
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
        boolean canRunGame =
                GLContext.getCapabilities().OpenGL20
                        && GLContext.getCapabilities().OpenGL11
                        && GLContext.getCapabilities().OpenGL12
                        && GLContext.getCapabilities().OpenGL14
                        && GLContext.getCapabilities().OpenGL15
                        && GLContext.getCapabilities().GL_ARB_framebuffer_object
                        && GLContext.getCapabilities().GL_ARB_texture_float
                        && GLContext.getCapabilities().GL_ARB_half_float_pixel
                        && GLContext.getCapabilities().GL_ARB_shader_objects;

        if (!canRunGame) {
            final String message = "Your GPU driver is not supporting the mandatory versions of OpenGL or some needed OpenGL extension. Considered updating your GPU drivers? Exiting...";

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
            if (!TerasologyEngine.isEditorInFocus()) {
                Mouse.setGrabbed(true);
            }
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
        config.save();
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
            if (!Display.isActive() && !TerasologyEngine.isEditorAttached()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("Display inactivity sleep interrupted", e);
                }
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
            audioManager.update(timer.getDelta());
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
            if (config.getRendering().isFullscreen()) {
                Display.setDisplayMode(Display.getDesktopDisplayMode());
                Display.setFullscreen(true);
            } else {
                Display.setDisplayMode(config.getRendering().getDisplayMode());
                Display.setResizable(true);
            }
        } catch (LWJGLException e) {
            logger.error("Can not initialize graphics device.", e);
            System.exit(1);
        }
    }

    public boolean isFullscreen() {
        return config.getRendering().isFullscreen();
    }

    public void setFullscreen(boolean state) {
        if (config.getRendering().isFullscreen() != state) {
            config.getRendering().setFullscreen(state);
            setDisplayMode();
            resizeViewport();
            CoreRegistry.get(GUIManager.class).update(true);
        }
    }

    public void setCustomViewPort(Canvas viewPort) {
        customViewPort = viewPort;
    }

    public static void setEditorInFocus(boolean focus) {
        editorInFocus = focus;
    }

    public static boolean isEditorInFocus() {
        return editorInFocus;
    }

    public static void setEditorAttached(boolean attached) {
        editorAttached = attached;
    }

    public static boolean isEditorAttached() {
        return editorAttached;
    }
}

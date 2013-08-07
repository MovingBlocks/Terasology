/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.engine;

import com.google.common.collect.Sets;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.audio.AudioManager;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.audio.openAL.OpenALManager;
import org.terasology.config.Config;
import org.terasology.engine.internal.TimeLwjgl;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.Game;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.logic.manager.GUIManager;
import org.terasology.engine.module.ModuleSecurityManager;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.gui.AdvancedMonitor;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.VertexBufferObjectManager;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontData;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.opengl.GLSLShader;
import org.terasology.rendering.opengl.OpenGLFont;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.rendering.opengl.OpenGLTexture;
import org.terasology.utilities.NativeHelper;
import org.terasology.version.TerasologyVersion;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Set;
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

    private EngineTime time;
    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private Canvas customViewPort = null;
    private boolean hibernationAllowed = false;
    private boolean gameFocused = true;
    private Set<StateChangeSubscriber> stateChangeSubscribers = Sets.newLinkedHashSet();

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
        logger.info("Home path: {}", PathManager.getInstance().getHomePath());
        logger.info("Install path: {}", PathManager.getInstance().getInstallPath());

        initConfig();

        initNativeLibs();
        initDisplay();
        initOpenGL();
        initOpenAL();
        initControls();
        initTimer(); // Dependent on LWJGL
        initManagers();
        updateInputConfig();
        CoreRegistry.putPermanently(GUIManager.class, new GUIManager(this));
        initSecurity();

        if (config.getSystem().isMonitoringEnabled()) {
            new AdvancedMonitor().setVisible(true);
        }
        initialised = true;
    }

    private void initSecurity() {
        // TODO: More work on security
        ModuleSecurityManager moduleSecurityManager = new ModuleSecurityManager();
        //System.setSecurityManager(moduleSecurityManager);
        moduleSecurityManager.addModAvailableClass(GUIManager.class);
        // TODO: Add in module available classes

    }

    private void initConfig() {
        if (Files.isRegularFile(Config.getConfigFile())) {
            try {
                config = Config.load(Config.getConfigFile());
            } catch (IOException e) {
                logger.error("Failed to load config", e);
                config = new Config();
            }
        } else {
            config = new Config();
        }
        if (!config.getDefaultModSelection().hasMod("core")) {
            config.getDefaultModSelection().addMod("core");
        }
        if (config.getSecurity().getServerPrivateCertificate() == null) {
            CertificateGenerator generator = new CertificateGenerator();
            CertificatePair serverIdentity = generator.generateSelfSigned();
            config.getSecurity().setServerCredentials(serverIdentity.getPublicCert(), serverIdentity.getPrivateCert());
            config.save();
        }
        CoreRegistry.putPermanently(Config.class, config);
    }

    private void updateInputConfig() {
        config.getInput().getBinds().updateForChangedMods();
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
        CoreRegistry.putPermanently(GameEngine.class, this);
        if (!initialised) {
            init();
        }
        changeState(initialState);
        running = true;
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

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
        if (currentState != null) {
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
                Thread.currentThread().setName("Engine-Task-Pool");
                try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getClass().getSimpleName())) {
                    task.run();
                } catch (RejectedExecutionException e) {
                    ThreadMonitor.addError(e);
                    logger.error("Thread submitted after shutdown requested: {}", name);
                } catch (Throwable e) {
                    ThreadMonitor.addError(e);
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
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("macosx"));
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                NativeHelper.addLibraryPath((PathManager.getInstance().getNativesPath().resolve("linux")));
                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("openal64");
                } else {
                    System.loadLibrary("openal");
                }
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("windows"));

                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("OpenAL64");
                } else {
                    System.loadLibrary("OpenAL32");
                }
                try {
                    OculusVrHelper.loadNatives();
                } catch (UnsatisfiedLinkError e) {
                    logger.warn("Could not load optional TeraOVR native libraries - Oculus support disabled");
                }
                break;
            default:
                logger.error("Unsupported operating system: {}", LWJGLUtil.getPlatformName());
                System.exit(1);
        }
    }


    private void initOpenAL() {
        if (config.getAudio().isDisableSound()) {
            audioManager = new NullAudioManager();
        } else {
            audioManager = new OpenALManager(config.getAudio());
        }
        CoreRegistry.putPermanently(AudioManager.class, audioManager);
        AssetManager.getInstance().setAssetFactory(AssetType.SOUND, audioManager.getStaticSoundFactory());
        AssetManager.getInstance().setAssetFactory(AssetType.MUSIC, audioManager.getStreamingSoundFactory());
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
        AssetManager.getInstance().setAssetFactory(AssetType.TEXTURE, new AssetFactory<TextureData, Texture>() {
            @Override
            public Texture buildAsset(AssetUri uri, TextureData data) {
                return new OpenGLTexture(uri, data);
            }
        });
        AssetManager.getInstance().setAssetFactory(AssetType.FONT, new AssetFactory<FontData, Font>() {
            @Override
            public Font buildAsset(AssetUri uri, FontData data) {
                return new OpenGLFont(uri, data);
            }
        });
        AssetManager.getInstance().setAssetFactory(AssetType.SHADER, new AssetFactory<ShaderData, Shader>() {
            @Override
            public Shader buildAsset(AssetUri uri, ShaderData data) {
                return new GLSLShader(uri, data);
            }
        });
        AssetManager.getInstance().setAssetFactory(AssetType.MATERIAL, new AssetFactory<MaterialData, Material>() {
            @Override
            public Material buildAsset(AssetUri uri, MaterialData data) {
                return new GLSLMaterial(uri, data);
            }
        });
        AssetManager.getInstance().setAssetFactory(AssetType.MESH, new AssetFactory<MeshData, Mesh>() {
            @Override
            public Mesh buildAsset(AssetUri uri, MeshData data) {
                return new OpenGLMesh(uri, data);
            }
        });
        AssetManager.getInstance().setAssetFactory(AssetType.SKELETON_MESH, new AssetFactory<SkeletalMeshData, SkeletalMesh>() {
            @Override
            public SkeletalMesh buildAsset(AssetUri uri, SkeletalMeshData data) {
                return new OpenGLSkeletalMesh(uri, data);
            }
        });
        AssetManager.getInstance().setAssetFactory(AssetType.ANIMATION, new AssetFactory<MeshAnimationData, MeshAnimation>() {
            @Override
            public MeshAnimation buildAsset(AssetUri uri, MeshAnimationData data) {
                return new MeshAnimationImpl(uri, data);
            }
        });
        CoreRegistry.putPermanently(ShaderManager.class, new ShaderManager());

    }

    private void checkOpenGL() {
        boolean canRunGame = GLContext.getCapabilities().OpenGL11
                & GLContext.getCapabilities().OpenGL12
                & GLContext.getCapabilities().OpenGL14
                & GLContext.getCapabilities().OpenGL15
                & GLContext.getCapabilities().GL_ARB_framebuffer_object
                & GLContext.getCapabilities().GL_ARB_texture_float
                & GLContext.getCapabilities().GL_ARB_half_float_pixel
                & GLContext.getCapabilities().GL_ARB_shader_objects;

        if (!canRunGame) {
            String message = "Your GPU driver is not supporting the mandatory versions or extensions of OpenGL. Considered updating your GPU drivers? Exiting...";
            logger.error(message);
            JOptionPane.showMessageDialog(null, message, "Mandatory OpenGL version(s) or extension(s) not supported", JOptionPane.ERROR_MESSAGE);
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
            Mouse.setGrabbed(false);
        } catch (LWJGLException e) {
            logger.error("Could not initialize controls.", e);
            System.exit(1);
        }
    }

    private void initManagers() {
        CoreRegistry.putPermanently(CollisionGroupManager.class, new CollisionGroupManager());
        CoreRegistry.putPermanently(ModuleManager.class, new ModuleManager());
        CoreRegistry.putPermanently(ComponentSystemManager.class, new ComponentSystemManager());
        CoreRegistry.putPermanently(NetworkSystem.class, new NetworkSystemImpl(time));
        CoreRegistry.putPermanently(Game.class, new Game(time));

        AssetType.registerAssetTypes();
        ClasspathSource source = new ClasspathSource(ModuleManager.ENGINE_MODULE,
                getClass().getProtectionDomain().getCodeSource(), ModuleManager.ASSETS_SUBDIRECTORY, ModuleManager.OVERRIDES_SUBDIRECTORY);
        AssetManager.getInstance().addAssetSource(source);
        CoreRegistry.get(ShaderManager.class).initShaders();

        VertexBufferObjectManager.getInstance();
    }

    private void initTimer() {
        time = new TimeLwjgl();
        CoreRegistry.putPermanently(Time.class, time);
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
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);

        PerformanceMonitor.startActivity("Other");
        // MAIN GAME LOOP
        while (running && !Display.isCloseRequested()) {

            // Only process rendering and updating once a second
            if (!Display.isActive() && isHibernationAllowed()) {
                time.setPaused(true);
                Iterator<Float> updateCycles = time.tick();
                while (updateCycles.hasNext()) {
                    updateCycles.next();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.warn("Display inactivity sleep interrupted", e);
                }

                Display.processMessages();
                time.setPaused(false);
                continue;
            }

            processStateChanges();

            if (currentState == null) {
                shutdown();
                break;
            }

            Iterator<Float> updateCycles = time.tick();

            PerformanceMonitor.startActivity("Network Update");
            networkSystem.update();
            PerformanceMonitor.endActivity();

            long totalDelta = 0;
            while (updateCycles.hasNext()) {
                float delta = updateCycles.next();
                totalDelta += time.getDeltaInMs();
                PerformanceMonitor.startActivity("Main Update");
                currentState.update(delta);
                PerformanceMonitor.endActivity();
            }

            PerformanceMonitor.startActivity("Render");
            currentState.render();
            Display.update();
            Display.sync(60);
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Audio");
            audioManager.update(totalDelta / 1000f);
            PerformanceMonitor.endActivity();

            PerformanceMonitor.startActivity("Input");
            currentState.handleInput(totalDelta / 1000f);
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
        for (StateChangeSubscriber subscriber : stateChangeSubscribers) {
            subscriber.onStateChange();
        }
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

    public boolean isHibernationAllowed() {
        return hibernationAllowed && currentState.isHibernationAllowed();
    }

    public void setHibernationAllowed(boolean allowed) {
        this.hibernationAllowed = allowed;
    }

    public boolean hasFocus() {
        return gameFocused && Display.isActive();
    }

    public void setFocus(boolean focused) {
        gameFocused = focused;
    }

    @Override
    public void subscribeToStateChange(StateChangeSubscriber subscriber) {
        stateChangeSubscribers.add(subscriber);
    }

    @Override
    public void unsubscribeToStateChange(StateChangeSubscriber subscriber) {
        stateChangeSubscribers.remove(subscriber);
    }

    public void setCustomViewport(Canvas canvas) {
        this.customViewPort = canvas;
    }
}

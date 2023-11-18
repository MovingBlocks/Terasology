// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.PathManagerProvider;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.TerasologyEngineBuilder;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.modes.StateIngame;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.headless.HeadlessAudio;
import org.terasology.engine.core.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.core.subsystem.headless.HeadlessInput;
import org.terasology.engine.core.subsystem.headless.HeadlessTimer;
import org.terasology.engine.core.subsystem.headless.mode.HeadlessStateChangeListener;
import org.terasology.engine.core.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.core.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.core.subsystem.lwjgl.LwjglTimer;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.network.JoinStatus;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.opengl.ScreenGrabber;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

/**
 * Manages game engines for tests.
 * <p>
 * There is always one engine that serves as the host. There may also be additional engines
 * simulating remote clients.
 * <p>
 * Most tests run with a single host and do not need to make direct references to this class.
 * <p>
 * This class is available via dependency injection with the {@link org.terasology.engine.registry.In} annotation
 * or as a parameter to a JUnit {@link org.junit.jupiter.api.Test} method; see {@link MTEExtension}.
 *
 * <h2>Client Engine Instances</h2>
 * Client instances can be easily created via {@link #createClient} which returns the in-game context of the created
 * engine instance. When this method returns, the client will be in the {@link StateIngame} state and connected to the
 * host. Currently all engine instances are headless, though it is possible to use headed engines in the future.
 */
public class Engines {
    public static final String DEFAULT_WORLD_GENERATOR = ModuleTestingEnvironment.DEFAULT_WORLD_GENERATOR;

    private static final Logger logger = LoggerFactory.getLogger(Engines.class);

    protected final Set<String> dependencies = Sets.newHashSet();
    protected String worldGeneratorUri = DEFAULT_WORLD_GENERATOR;
    protected boolean doneLoading;
    protected Context hostContext;
    protected final List<TerasologyEngine> engines = Lists.newArrayList();
    protected final List<Class<? extends EngineSubsystem>> subsystems = Lists.newArrayList();

    PathManager pathManager;
    PathManagerProvider.Cleaner pathManagerCleaner;
    TerasologyEngine host;
    private final NetworkMode networkMode;

    public Engines(List<String> dependencies, String worldGeneratorUri, NetworkMode networkMode,
                   List<Class<? extends EngineSubsystem>> subsystems) {
        this.networkMode = networkMode;
        this.dependencies.addAll(dependencies);
        this.subsystems.addAll(subsystems);

        if (worldGeneratorUri != null) {
            this.worldGeneratorUri = worldGeneratorUri;
        }
    }

    /**
     * Set up and start the engine as configured via this environment.
     * <p>
     * Every instance should be shut down properly by calling {@link #tearDown()}.
     */
    public void setup() {
        mockPathManager();
        try {
            host = createHost(networkMode);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ScreenGrabber grabber = Mockito.mock(ScreenGrabber.class);
        hostContext.put(ScreenGrabber.class, grabber);
        CoreRegistry.put(GameEngine.class, host);
    }

    /**
     * Shut down a previously started testing environment.
     * <p>
     * Used to properly shut down and clean up a testing environment set up and started with {@link #setup()}.
     */
    public void tearDown() {
        engines.forEach(TerasologyEngine::shutdown);
        engines.forEach(TerasologyEngine::cleanup);
        engines.clear();
        try {
            pathManagerCleaner.close();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        host = null;
        hostContext = null;
    }

    /**
     * Creates a new client and connects it to the host.
     * <p>
     * Requires the host to have a {@link NetworkMode} that accepts connections.
     * Configure the host's network mode using
     * {@link IntegrationEnvironment#networkMode @IntegrationEnvironment#networkmode}
     * on your test class.
     *
     * @return the created client's context object
     */
    public Context createClient(MainLoop mainLoop) throws IOException {
        TerasologyEngine client = createHeadlessEngine();

        client.changeState(new StateMainMenu());
        if (!connectToHost(client, mainLoop)) {
            throw new RuntimeException(String.format("Could not connect client %s to local host - timeout.", client));
        }
        Context context = client.getState().getContext();
        context.put(ScreenGrabber.class, hostContext.get(ScreenGrabber.class));

        logger.info("Created client: {}", client);

        return client.getState().getContext();
    }

    /**
     * The engines active in this instance of the module testing environment.
     * <p>
     * Engines are created for the host and connecting clients.
     *
     * @return list of active engines
     */
    public List<TerasologyEngine> getEngines() {
        return Lists.newArrayList(engines);
    }

    /**
     * Get the host context for this module testing environment.
     * <p>
     * The host context will be null if the testing environment has not been set up via {@link #setup()}
     * beforehand.
     *
     * @return the engine's host context, or null if not set up yet
     */
    public Context getHostContext() {
        return hostContext;
    }

    TerasologyEngine createHeadlessEngine() throws IOException {
        TerasologyEngineBuilder terasologyEngineBuilder = new TerasologyEngineBuilder();
        terasologyEngineBuilder
                .add(new IntegrationEnvironmentSubsystem())
                .add(new HeadlessGraphics())
                .add(new HeadlessTimer())
                .add(new HeadlessAudio())
                .add(new HeadlessInput());
        createExtraSubsystems().forEach(terasologyEngineBuilder::add);

        return createEngine(terasologyEngineBuilder);
    }

    @SuppressWarnings("unused")
    TerasologyEngine createHeadedEngine() throws IOException {
        EngineSubsystem audio = new LwjglAudio();
        TerasologyEngineBuilder terasologyEngineBuilder = new TerasologyEngineBuilder()
                .add(new IntegrationEnvironmentSubsystem())
                .add(audio)
                .add(new LwjglGraphics())
                .add(new LwjglTimer())
                .add(new LwjglInput());
        createExtraSubsystems().forEach(terasologyEngineBuilder::add);

        return createEngine(terasologyEngineBuilder);
    }

    List<EngineSubsystem> createExtraSubsystems()  {
        List<EngineSubsystem> instances = new ArrayList<>();
        for (Class<? extends EngineSubsystem> clazz : subsystems) {
            try {
                EngineSubsystem subsystem = newInstance(clazz);
                instances.add(subsystem);
                logger.debug("Created new {}", subsystem);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed creating new " + clazz.getName(), e);
            }
        }
        return instances;
    }

    TerasologyEngine createEngine(TerasologyEngineBuilder terasologyEngineBuilder) throws IOException {
        System.setProperty(ModuleManager.LOAD_CLASSPATH_MODULES_PROPERTY, "true");

        // create temporary home paths so the MTE engines don't overwrite config/save files in your real home path
        // FIXME: Collisions when attempting to do multiple simultaneous createEngines.
        //    (PathManager will need to be set in Context, not a process-wide global.)
        Path path = Files.createTempDirectory("terasology-mte-engine");
        PathManager.getInstance().useOverrideHomePath(path);
        logger.info("Created temporary engine home path: {}", path);

        // JVM will delete these on normal termination but not exceptions.
        path.toFile().deleteOnExit();

        TerasologyEngine terasologyEngine = terasologyEngineBuilder.build();
        terasologyEngine.initialize();

        engines.add(terasologyEngine);
        return terasologyEngine;
    }

    protected void mockPathManager() {
        PathManager originalPathManager = PathManager.getInstance();
        if (!Mockito.mockingDetails(originalPathManager).isMock()) {
            pathManager = Mockito.spy(originalPathManager);
        } else {
            pathManager = originalPathManager;
        }
        Mockito.when(pathManager.getModulePaths()).thenReturn(Collections.emptyList());
        pathManagerCleaner = new PathManagerProvider.Cleaner(originalPathManager, pathManager);
        PathManagerProvider.setPathManager(pathManager);
    }

    TerasologyEngine createHost(NetworkMode hostNetworkMode) throws IOException {
        TerasologyEngine host = createHeadlessEngine();
        host.subscribeToStateChange(new HeadlessStateChangeListener(host));
        host.changeState(new TestingStateHeadlessSetup(dependencies, worldGeneratorUri, hostNetworkMode));

        doneLoading = false;
        host.subscribeToStateChange(() -> {
            GameState newState = host.getState();
            logger.debug("New engine state is {}", newState);
            if (newState instanceof StateIngame) {
                hostContext = newState.getContext();
                if (hostContext == null) {
                    logger.warn("hostContext is NULL in engine state {}", newState);
                }
                doneLoading = true;
            } else if (newState instanceof StateLoading) {
                CoreRegistry.put(GameEngine.class, host);
            }
        });

        boolean keepTicking;
        while (!doneLoading) {
            keepTicking = host.tick();
            if (!keepTicking) {
                throw new RuntimeException(String.format(
                        "Engine stopped ticking before we got in game. Current state: %s",
                        host.getState()
                ));
            }
        }

        logger.info("Created host: {}", host);

        return host;
    }

    /**
     *
     * @param client the client engine to connect to the local host
     * @param mainLoop
     *
     * return true if the connection to the host was successful and the client is in state <i>in-game</i>.
     */
    boolean connectToHost(TerasologyEngine client, MainLoop mainLoop) {
        CoreRegistry.put(Config.class, client.getFromEngineContext(Config.class));
        JoinStatus joinStatus = null;
        try {
            joinStatus = client.getFromEngineContext(NetworkSystem.class).join("localhost", 25777);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while joining: ", e);
        }

        client.changeState(new StateLoading(joinStatus));
        CoreRegistry.put(GameEngine.class, client);

        // TODO: subscribe to state change and return an asynchronous result
        //     so that we don't need to pass mainLoop to here.
        return !mainLoop.runUntil(() -> client.getState() instanceof StateIngame);
    }
}

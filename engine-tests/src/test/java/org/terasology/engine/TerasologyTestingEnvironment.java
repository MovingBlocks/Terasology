// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import com.badlogic.gdx.physics.bullet.Bullet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.game.Game;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.internal.ReadWriteStorageManager;
import org.terasology.engine.recording.CharacterStateEventPositionMap;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;

/**
 * A base class for unit test classes to inherit to run in a Terasology environment - with LWJGL set up and so forth
 */
public abstract class TerasologyTestingEnvironment {
    protected static Context context;

    private static final Logger logger = LoggerFactory.getLogger(TerasologyTestingEnvironment.class);

    private static ModuleManager moduleManager;

    private static HeadlessEnvironment env;

    protected EngineTime mockTime;

    private EngineEntityManager engineEntityManager;

    @BeforeAll
    public static void setupEnvironment(@TempDir Path tempHome) throws Exception {
        PathManager.getInstance().useOverrideHomePath(tempHome);
        Bullet.init(true, false);

        /*
         * Create at least for each class a new headless environment as it is fast and prevents side effects
         * (Reusing a headless environment after other tests have modified the core registry isn't really clean)
         */
        env = new HeadlessEnvironment(new Name("engine"), new Name("unittest"));
        context = env.getContext();
        CoreRegistry.setContext(context);
        moduleManager = context.get(ModuleManager.class);

    }

    @BeforeEach
    public void setup() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.with(ModuleManager.class).lifetime(Lifetime.Singleton).use(() -> moduleManager);

        mockTime = mock(EngineTime.class);
        serviceRegistry.with(Time.class).lifetime(Lifetime.Singleton).use(() -> mockTime);
        NetworkSystemImpl networkSystem = new NetworkSystemImpl(mockTime, context);
        serviceRegistry.with(Game.class).lifetime(Lifetime.Singleton).use(Game::new);
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(() -> networkSystem);
        EntitySystemSetupUtil.addReflectionBasedLibraries(serviceRegistry);

        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        serviceRegistry.with(RecordAndReplayUtils.class).lifetime(Lifetime.Singleton).use(() -> recordAndReplayUtils);
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        serviceRegistry.with(CharacterStateEventPositionMap.class).lifetime(Lifetime.Singleton).use(() -> characterStateEventPositionMap);
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        serviceRegistry.with(DirectionAndOriginPosRecorderList.class).lifetime(Lifetime.Singleton).use(() -> directionAndOriginPosRecorderList);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(serviceRegistry);

        Game game = new Game();
        game.load(new GameManifest("world1", "world1", 0));
        serviceRegistry.with(StorageManager.class).lifetime(Lifetime.Singleton).use(ReadWriteStorageManager.class);

        serviceRegistry.with(ComponentSystemManager.class).lifetime(Lifetime.Singleton).use(ComponentSystemManager.class);
        serviceRegistry.with(Console.class).lifetime(Lifetime.Singleton).use(ConsoleImpl.class);
        context = new ContextImpl(context, serviceRegistry);
        engineEntityManager = context.get(EngineEntityManager.class);
        EntitySystemSetupUtil.configureEntityManagementRelatedClasses(context.get(TypeHandlerLibrary.class),
                context.get(EntitySystemLibrary.class), moduleManager.getEnvironment(), engineEntityManager);

        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
        context.get(ComponentSystemManager.class).initialise();
        CoreRegistry.setContext(context);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (env != null) {
            env.close();
        } else {
            logger.warn("TTE.env unexpectedly null during tearDown");
        }
    }


    public EngineEntityManager getEntityManager() {
        return engineEntityManager;
    }
}

/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.documentation;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.terasology.HeadlessEnvironment;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.Game;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.recording.*;
import org.terasology.reflection.TypeRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * Environment with a MapWorldProvider and BlockManager. Useful to get headless environment with a generated world.
 */
public class HEnv extends HeadlessEnvironment {
    //protected static Context context;
    protected static EngineTime mockTime;
    //private static HeadlessEnvironment env;
    private static ModuleManager moduleManager;
    //private static EngineEntityManager engineEntityManager;

    public HEnv(Name... modules) throws IOException {
        super(modules);
        initialize();
    }

    private void initialize() throws IOException {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));
        /*
         * Create at least for each class a new headless environemnt as it is fast and prevents side effects
         * (Reusing a headless environment after other tests have modified the core registry isn't really clean)
         */
        //env = new HeadlessEnvironment(new Name("engine"));
        //context = env.getContext();
        moduleManager = context.get(ModuleManager.class);

        context.put(ModuleManager.class, moduleManager);
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);

        mockTime = mock(EngineTime.class);
        context.put(Time.class, mockTime);
        NetworkSystemImpl networkSystem = new NetworkSystemImpl(mockTime, context);
        context.put(Game.class, new Game());
        context.put(NetworkSystem.class, networkSystem);
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        //engineEntityManager = context.get(EngineEntityManager.class);
        BlockManager mockBlockManager = context.get(BlockManager.class); // 'mock' added to avoid hiding a field
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);
        RecordedEventStore recordedEventStore = new RecordedEventStore();
        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        context.put(RecordAndReplayUtils.class, recordAndReplayUtils);
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        context.put(CharacterStateEventPositionMap.class, characterStateEventPositionMap);
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        context.put(DirectionAndOriginPosRecorderList.class, directionAndOriginPosRecorderList);
        //RecordAndReplaySerializer recordAndReplaySerializer = new RecordAndReplaySerializer(engineEntityManager, recordedEventStore, recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList, moduleManager, context.get(TypeRegistry.class));
        //context.put(RecordAndReplaySerializer.class, recordAndReplaySerializer);

        //Path savePath = PathManager.getInstance().getSavePath("world1");
        //context.put(StorageManager.class, new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(),
        //       engineEntityManager, mockBlockManager, extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus));

        ComponentSystemManager componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);
        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
        context.get(ComponentSystemManager.class).initialise();
        context.put(Console.class, new ConsoleImpl(context));

        this.setupAssetManager();
    }

    @Override
    protected void setupModuleManager(Set<Name> moduleNames) throws Exception {
        TypeRegistry typeRegistry = new TypeRegistry();
        context.put(TypeRegistry.class, typeRegistry);

        ModuleManager moduleManager = ModuleManagerFactory.create("meta.terasology.org");
        ModuleRegistry registry = moduleManager.getRegistry();

        DependencyResolver resolver = new DependencyResolver(registry);
        ResolutionResult result = resolver.resolve(moduleNames);

        if (result.isSuccess()) {
            ModuleEnvironment modEnv = moduleManager.loadEnvironment(result.getModules(), true);
            typeRegistry.reload(modEnv);
            System.out.println("Loaded modules: " + modEnv.getModuleIdsOrderedByDependencies());
        } else {
            System.out.println("Could not resolve module dependencies for " + moduleNames);
        }

        context.put(ModuleManager.class, moduleManager);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
    }

    public ModuleManager getModelManager() {
        return moduleManager;
    }

    public Context getContext() {
        return context;
    }
}
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
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.game.Game;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.naming.Name;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.ReadWriteStorageManager;
import org.terasology.recording.*;
import org.terasology.reflection.TypeRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;

import java.nio.file.FileSystem;
import java.nio.file.Path;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.codehaus.plexus.util.StringUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.terasology.HeadlessEnvironment;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.SelectModulesConfig;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.*;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ExtraDataModuleExtension;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.RemoteModuleExtension;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.headless.HeadlessAudio;
import org.terasology.engine.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.subsystem.headless.HeadlessInput;
import org.terasology.engine.subsystem.headless.HeadlessTimer;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.game.Game;
import org.terasology.i18n.TranslationSystem;
import org.terasology.i18n.TranslationSystemImpl;
import org.terasology.input.*;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.*;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.ReadWriteStorageManager;
import org.terasology.recording.*;
import org.terasology.reflection.TypeRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.UniverseWrapper;
import org.terasology.rendering.nui.layers.mainMenu.gameDetailsScreen.ModuleSelectionInfo;
import org.terasology.rendering.nui.widgets.ResettableUIText;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.BlockLifecycleEvent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.lang.annotation.Annotation;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Environment with a MapWorldProvider and BlockManager. Useful to get headless environment with a generated world.
 */
public class HEnv extends HeadlessEnvironment {

    //protected static Context context;

    private static ModuleManager moduleManager;

    //private static HeadlessEnvironment env;

    protected static EngineTime mockTime;
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
        //this.setupAssetManager();
    }

    public ModuleManager getModelManager() {
        return moduleManager;
    }

    public Context getContext(){
        return context;
    }

}
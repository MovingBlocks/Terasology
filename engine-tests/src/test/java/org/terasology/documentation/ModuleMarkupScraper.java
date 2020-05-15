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

import static org.mockito.Mockito.mock;

public final class ModuleMarkupScraper {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_GITHUB_MODULE_URL = "https://github.com/Terasology/";
    private static final List INTERNAL_MODULES = Arrays.asList("Core", "engine", "CoreSampleGameplay", "BuilderSampleGameplay", "BiomesAPI");

    protected static Context context;

    private static ModuleManager moduleManager;

    private static HeadlessEnvironment env;
//
//    protected static EngineTime mockTime;
//    private static EngineEntityManager engineEntityManager;

    private ModuleMarkupScraper() throws IOException {
        super();

    }

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {

        env = new HEnv(new Name("engine"));
        context = env.getContext();


        TranslationSystemImpl translationSystem = new TranslationSystemImpl(context);//engine.createChildContext()

        //context.put(TranslationSystem.class, translationSystem);
        moduleManager = context.get(ModuleManager.class);

     //   Path homePath = Paths.get("");
     //   PathManager.getInstance().useOverrideHomePath(homePath);
//        ModuleManager moduleManager = ModuleManagerFactory.create();

        TerasologyEngineBuilder builder = new TerasologyEngineBuilder();
        builder.add(new HeadlessGraphics())
                .add(new HeadlessTimer())
                .add(new HeadlessAudio())
                .add(new HeadlessInput());
        TerasologyEngine engine = builder.build();
        engine.initialize();

//
//        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
//        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
//        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));
//        /*
//         * Create at least for each class a new headless environemnt as it is fast and prevents side effects
//         * (Reusing a headless environment after other tests have modified the core registry isn't really clean)
//         */
//        env = new HeadlessEnvironment(new Name("engine"));
//        context = env.getContext();
//        moduleManager = context.get(ModuleManager.class);
//
//        moduleManager.getInstallManager().updateRemoteRegistry();
//
//
//        ContextImpl context = new ContextImpl();
//        Config config = new Config(context);
//        config.loadDefaults();
//        context.put(Config.class, config);
//
//        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
//        assetTypeManager.registerCoreAssetType(Prefab.class,
//                (AssetFactory<Prefab, PrefabData>) PojoPrefab::new, "prefabs");
//
//        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
//        context.put(AssetManager.class, assetTypeManager.getAssetManager());

        //ModuleEnvironment environment = moduleManager.getEnvironment();

        DependencyResolver dependencyResolver = new DependencyResolver(moduleManager.getRegistry());

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {



            //            if(moduleId.toString().equals("engine") || moduleId.toString().equals("unittest") )
//                continue;


//                if(!moduleId.toString().equals("Workstation"))
//                    continue;

            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);



            if (module.isCodeModule()) {
                System.out.println("Examining Module: " + moduleId);

                String moduleDescription = getModuleDescription(moduleManager, translationSystem, module);

                System.out.println("  Module Description: " + moduleDescription);

//                ModuleMetadata moduleMetadata = module.getMetadata();
//                if (moduleMetadata != null) {
//
//                    String moduleDescription = getModuleDescription(translationSystem, moduleMetadata);
//                    System.out.println("   Dependencies: " + moduleDescription);
//
//                    StringBuilder dependenciesNames;
//                    List<DependencyInfo> dependencies = moduleMetadata.getDependencies();
//                    if (dependencies != null && !dependencies.isEmpty()) {
//                        dependenciesNames = new StringBuilder();
//                        for (DependencyInfo dependency : dependencies) {
//                            if(dependenciesNames.length() > 0)
//                                dependenciesNames.append(", ");
//                            dependenciesNames.append(dependency.getId().toString());
//                        }
//                    } else {
//                        dependenciesNames = new StringBuilder();
//                    }
//                    System.out.println("   Dependencies: " + dependenciesNames);
//                }


                Set<Module> modules = new HashSet<>();
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    modules = result.getModules();
                } else {
                    modules.add(module);
                }

                try (ModuleEnvironment environment2 = moduleManager.loadEnvironment(modules, false)) {
                    for (Class<? extends Event> type : environment2.getSubtypesOf(Event.class)) {
                        Name mod = environment2.getModuleProviding(type);

                        if(!mod.toString().equals(moduleId.toString()))
                            continue;

                        System.out.println("  Event: " + type.getSimpleName() + " | From Module: " + mod);

                        // Are there any other classes derived from this class?
                        for (Class<? extends Object> eventType : environment2.getSubtypesOf(type)) {
                            System.out.println("    " + eventType.getName() + " | Event Module: " + environment2.getModuleProviding(eventType));
                        }
                    }
                }
            }
        }
        env.close();
    }


//    public static void setupEnvironment() throws Exception {
//        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
//        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
//        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));
//        /*
//         * Create at least for each class a new headless environemnt as it is fast and prevents side effects
//         * (Reusing a headless environment after other tests have modified the core registry isn't really clean)
//         */
//        env = new HeadlessEnvironment(new Name("engine"));
//        context = env.getContext();
//        moduleManager = context.get(ModuleManager.class);
//
//
//
//        context.put(ModuleManager.class, moduleManager);
//        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);
//
//        mockTime = mock(EngineTime.class);
//        context.put(Time.class, mockTime);
//        NetworkSystemImpl networkSystem = new NetworkSystemImpl(mockTime, context);
//        context.put(Game.class, new Game());
//        context.put(NetworkSystem.class, networkSystem);
//        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
//        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
//        engineEntityManager = context.get(EngineEntityManager.class);
//        BlockManager mockBlockManager = context.get(BlockManager.class); // 'mock' added to avoid hiding a field
//        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);
//        RecordedEventStore recordedEventStore = new RecordedEventStore();
//        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
//        context.put(RecordAndReplayUtils.class, recordAndReplayUtils);
//        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
//        context.put(CharacterStateEventPositionMap.class, characterStateEventPositionMap);
//        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
//        context.put(DirectionAndOriginPosRecorderList.class, directionAndOriginPosRecorderList);
//        RecordAndReplaySerializer recordAndReplaySerializer = new RecordAndReplaySerializer(engineEntityManager, recordedEventStore, recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList, moduleManager, context.get(TypeRegistry.class));
//        context.put(RecordAndReplaySerializer.class, recordAndReplaySerializer);
//
//        Path savePath = PathManager.getInstance().getSavePath("world1");
//        context.put(StorageManager.class, new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(),
//                engineEntityManager, mockBlockManager, extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus));
//
//        ComponentSystemManager componentSystemManager = new ComponentSystemManager(context);
//        context.put(ComponentSystemManager.class, componentSystemManager);
//        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);
//
//        boolean complete = false;
//        prefabLoadStep.begin();
//        while (!complete) {
//            complete = prefabLoadStep.step();
//        }
//        context.get(ComponentSystemManager.class).initialise();
//        context.put(Console.class, new ConsoleImpl(context));
//
//
//    }
//
//    public static void tearDown() throws Exception {
//        env.close();
//    }

    private static String getModuleDescription(final ModuleManager moduleManager, final TranslationSystemImpl translationSystem, final Module module) {
        if (module == null || module.getMetadata() == null) {
            return "";
        }
        final ModuleMetadata metadata = module.getMetadata();

        StringBuilder dependenciesNames;
        final List<DependencyInfo> dependencies = metadata.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            dependenciesNames = new StringBuilder(translationSystem
                    .translate("${engine:menu#module-dependencies-exist}") + ":" + '\n');
            for (DependencyInfo dependency : dependencies) {
                dependenciesNames
                        .append("   ")
                        .append(dependency.getId().toString())
                        .append('\n');
            }
        } else {
            dependenciesNames = new StringBuilder(translationSystem
                    .translate("${engine:menu#module-dependencies-empty}") + ".");
        }

        return translationSystem.translate("${engine:menu#game-details-module-id}") + ": " +
                metadata.getId() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-display-name}") + ": " +
                metadata.getDisplayName() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-version}") + ": " +
                metadata.getVersion() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-description}") + ": " +
                metadata.getDescription() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-permissions}") + ": " +
                String.join(", ", metadata.getRequiredPermissions()) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-github}") + ": " +
                getOriginModuleUrl(module) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-author}") + ": " +
                ExtraDataModuleExtension.getAuthor(module) +
                '\n' +
//                translationSystem.translate("${engine:menu#game-details-module-size}") + ": " +
//                getRemoteSize(moduleManager, module) +
//                '\n' +
//                translationSystem.translate("${engine:menu#game-details-module-last-update-date}") + ": " +
//                getLastUpdateDate(moduleManager, module) +
//                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-categories}") + ": " +
                getModuleTags(module) +
                dependenciesNames;
    }

    private static String getOriginModuleUrl(Module module) {
        final String origin = ExtraDataModuleExtension.getOrigin(module);
        if (StringUtils.isBlank(origin) && !INTERNAL_MODULES.contains(module.getId().toString())) {
            return DEFAULT_GITHUB_MODULE_URL + module.getId();
        }
        return origin;
    }

    private static String getOnlineVersion(final ModuleManager moduleManager, final DependencyInfo dependencyInfo) {
        return moduleManager.getInstallManager().getRemoteRegistry().stream()
                .filter(module -> module.getId().equals(dependencyInfo.getId()))
                .findFirst()
                .map(Module::getVersion)
                .map(String::valueOf)
                .orElse("");
    }

//    private static String getRemoteSize(final ModuleManager moduleManager, final Module module) {
//        return moduleManager.getInstallManager().getRemoteRegistry().stream()
//                .filter(m -> m.getId().equals(module.getId()))
//                .findFirst()
//                .map(Module::getMetadata)
//                .map(RemoteModuleExtension::getArtifactSize)
//                .map(m -> m + " bytes")
//                .orElse("");
//    }

//    private static String getLastUpdateDate(final ModuleManager moduleManager, final Module module) {
//        return moduleManager.getInstallManager().getRemoteRegistry().stream()
//                .filter(m -> m.getId().equals(module.getId()))
//                .findFirst()
//                .map(Module::getMetadata)
//                .map(RemoteModuleExtension::getLastUpdated)
//                .map(dateFormat::format)
//                .orElse("");
//    }

    private static String getModuleTags(final Module module) {
        return StandardModuleExtension.booleanPropertySet().stream()
                .filter(ext -> ext.isProvidedBy(module))
                .map(StandardModuleExtension::getKey)
                .collect(Collectors.joining(", "));
    }
}


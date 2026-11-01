// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.nullAudio.NullAudioManager;
import org.terasology.engine.audio.nullAudio.NullSound;
import org.terasology.engine.audio.nullAudio.NullStreamingSound;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessMaterial;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessMesh;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessShader;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessSkeletalMesh;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessTexture;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.game.Game;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.internal.ReadWriteStorageManager;
import org.terasology.engine.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.engine.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.engine.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.CollisionGroupManager;
import org.terasology.engine.recording.CharacterStateEventPositionMap;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.engine.rendering.assets.atlas.Atlas;
import org.terasology.engine.rendering.assets.font.Font;
import org.terasology.engine.rendering.assets.font.FontImpl;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.assets.texture.PNGTextureFormat;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.BlockFamilyLibrary;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.shapes.BlockShapeImpl;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.engine.world.block.tiles.BlockTile;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.block.tiles.WorldAtlas;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.sun.BasicCelestialModel;
import org.terasology.engine.world.sun.CelestialModel;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.engine.world.sun.DefaultCelestialSystem;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeImpl;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Setup a headless ( = no graphics ) environment.
 * Based on TerasologyTestingEnvironment code.
 * <p>
 * <b>Deprecated</b> for use outside of {@code engine-tests}; modules should use ModuleTestingEnvironment.
 */
public class HeadlessEnvironment extends Environment {

    private static final WorldTime WORLD_TIME = new WorldTimeImpl();
    private static final Logger logger = LoggerFactory.getLogger(HeadlessEnvironment.class);

    /**
     * Setup a headless ( = no graphics ) environment
     *
     * @param modules a set of module names that should be loaded (latest version)
     */
    public HeadlessEnvironment(Name... modules) {
        super(modules);
    }

    @Override
    protected void setupStorageManager(ServiceRegistry serviceRegistry) throws IOException {
        Game game = new Game();
        game.load(new GameManifest("world1", "world1", 0));
        serviceRegistry.with(Game.class).lifetime(Lifetime.Singleton).use(() -> game);

        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        serviceRegistry.with(RecordAndReplayUtils.class).lifetime(Lifetime.Singleton).use(() -> recordAndReplayUtils);
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        serviceRegistry.with(CharacterStateEventPositionMap.class).lifetime(Lifetime.Singleton).use(() -> characterStateEventPositionMap);
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        serviceRegistry.with(DirectionAndOriginPosRecorderList.class).lifetime(Lifetime.Singleton).use(() -> directionAndOriginPosRecorderList);
        serviceRegistry.with(RecordAndReplaySerializer.class).lifetime(Lifetime.Singleton);

        serviceRegistry.with(BlockFamilyLibrary.class).lifetime(Lifetime.Singleton).use(BlockFamilyLibrary.class);
        serviceRegistry.with(StorageManager.class).lifetime(Lifetime.Singleton).use(ReadWriteStorageManager.class);
    }

    @Override
    protected void setupNetwork(ServiceRegistry serviceRegistry) {
        EngineTime mockTime = mock(EngineTime.class);
        serviceRegistry.with(Time.class).lifetime(Lifetime.Singleton).use(() -> mockTime);
        serviceRegistry.with(EngineTime.class).lifetime(Lifetime.Singleton).use(() -> mockTime);
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(NetworkSystemImpl.class);
    }

    @Override
    protected void setupEntitySystem(ServiceRegistry serviceRegistry) {
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(serviceRegistry);
    }

    @Override
    protected void setupCollisionManager(ServiceRegistry serviceRegistry) {
        CollisionGroupManager collisionGroupManager = new CollisionGroupManager();
        serviceRegistry.with(CollisionGroupManager.class).lifetime(Lifetime.Singleton).use(() -> collisionGroupManager);
    }

    @Override
    protected void registerCollisionTypeHandlers(Context context) {
        CollisionGroupManager collisionGroupManager = context.get(CollisionGroupManager.class);
        context.get(TypeHandlerLibrary.class).addTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler(collisionGroupManager));
    }

    @Override
    protected void setupBlockManager(ServiceRegistry serviceRegistry, AssetManager assetManager) {
        WorldAtlas worldAtlas = new NullWorldAtlas();
        BlockManagerImpl blockManager = new BlockManagerImpl(worldAtlas, assetManager);
        serviceRegistry.with(BlockManager.class).lifetime(Lifetime.Singleton).use(() -> blockManager);
    }

    @Override
    protected void registerBlockTypeHandlers(Context context) {
        BlockManager blockManager = context.get(BlockManager.class);
        TypeHandlerLibrary typeHandlerLibrary = context.get(TypeHandlerLibrary.class);
        typeHandlerLibrary.addTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler(blockManager));
        typeHandlerLibrary.addTypeHandler(Block.class, new BlockTypeHandler(blockManager));
    }

    @Override
    protected void setupExtraDataManager(ServiceRegistry serviceRegistry, Context context) {
        serviceRegistry.with(ExtraBlockDataManager.class).lifetime(Lifetime.Singleton).use(ExtraBlockDataManager.class);
    }

    @Override
    protected AssetManager setupEmptyAssetManager(ModuleManager moduleManager, ServiceRegistry serviceRegistry) {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());

        serviceRegistry.with(ModuleAwareAssetTypeManager.class).lifetime(Lifetime.Singleton).use(() -> assetTypeManager);
        serviceRegistry.with(AssetManager.class).lifetime(Lifetime.Singleton).use(() -> assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected AssetManager setupAssetManager(ModuleManager moduleManager, ServiceRegistry serviceRegistry) {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.createAssetType(Prefab.class,
                PojoPrefab::new, "prefabs");
        assetTypeManager.createAssetType(BlockShape.class,
                BlockShapeImpl::new, "shapes");
        assetTypeManager.createAssetType(BlockSounds.class,
                BlockSounds::new, "blockSounds");
        assetTypeManager.createAssetType(BlockTile.class,
                BlockTile::new, "blockTiles");
        AssetType<BlockFamilyDefinition, BlockFamilyDefinitionData> blockFamilyDefinitionDataAssetType =
                assetTypeManager.createAssetType(BlockFamilyDefinition.class,
                        BlockFamilyDefinition::new, "blocks");


        assetTypeManager.getAssetFileDataProducer(blockFamilyDefinitionDataAssetType).addAssetFormat(
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager()));

        assetTypeManager.createAssetType(StaticSound.class, NullSound::new, "sounds");
        assetTypeManager.createAssetType(StreamingSound.class, NullStreamingSound::new, "music");

        assetTypeManager.createAssetType(UISkinAsset.class,
                UISkinAsset::new, "skins");
        assetTypeManager.createAssetType(BehaviorTree.class,
                BehaviorTree::new, "behaviors");
        assetTypeManager.createAssetType(UIElement.class,
                UIElement::new, "ui");
        assetTypeManager.createAssetType(Font.class,
                FontImpl::new, "fonts");
        AssetType<Texture, TextureData> textureDataAssetType = assetTypeManager.createAssetType(Texture.class,
                HeadlessTexture::create, "textures", "fonts");
        assetTypeManager.getAssetFileDataProducer(textureDataAssetType).addAssetFormat(
                new PNGTextureFormat(Texture.FilterMode.NEAREST,
                        path -> path.getPath().get(1).equals("textures")));
        assetTypeManager.getAssetFileDataProducer(textureDataAssetType).addAssetFormat(
                new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getPath().get(1).equals("fonts")));

        assetTypeManager.createAssetType(Shader.class,
                HeadlessShader::new, "shaders");
        assetTypeManager.createAssetType(Material.class,
                HeadlessMaterial::new, "materials");
        assetTypeManager.createAssetType(Mesh.class,
                HeadlessMesh::new, "mesh");
        assetTypeManager.createAssetType(SkeletalMesh.class,
                HeadlessSkeletalMesh::new, "skeletalMesh");
        assetTypeManager.createAssetType(MeshAnimation.class,
                MeshAnimationImpl::new, "animations");

        assetTypeManager.createAssetType(Atlas.class,
                Atlas::new, "atlas");
        assetTypeManager.createAssetType(Subtexture.class,
                Subtexture::new);

        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());

        serviceRegistry.with(ModuleAwareAssetTypeManager.class).lifetime(Lifetime.Singleton).use(() -> assetTypeManager);
        serviceRegistry.with(AssetManager.class).lifetime(Lifetime.Singleton).use(() -> assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected void setupAudio(ServiceRegistry serviceRegistry) {
        NullAudioManager audioManager = new NullAudioManager();
        serviceRegistry.with(AudioManager.class).lifetime(Lifetime.Singleton).use(() -> audioManager);
    }

    @Override
    protected void setupConfig(ServiceRegistry serviceRegistry) {
        Config config = new Config();
        config.loadDefaults();
        serviceRegistry.with(Config.class).lifetime(Lifetime.Singleton).use(() -> config);
        StorageServiceWorker storageServiceWorker = mock(StorageServiceWorker.class);
        serviceRegistry.with(StorageServiceWorker.class).lifetime(Lifetime.Singleton).use(() -> storageServiceWorker);
        PlayerConfig playerConfig = mock(PlayerConfig.class);
        serviceRegistry.with(PlayerConfig.class).lifetime(Lifetime.Singleton).use(() -> playerConfig);
    }

    @Override
    protected ModuleManager setupModuleManager(ServiceRegistry serviceRegistry, Set<Name> moduleNames) throws RuntimeException {
        TypeRegistry.WHITELISTED_CLASSES = ExternalApiWhitelist.CLASSES.stream().map(Class::getName).collect(Collectors.toSet());

        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleTypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());

        serviceRegistry.with(TypeRegistry.class).lifetime(Lifetime.Singleton).use(() -> typeRegistry);
        serviceRegistry.with(ModuleTypeRegistry.class).lifetime(Lifetime.Singleton).use(() -> typeRegistry);

        moduleManager.resolveAndLoadEnvironment(context, moduleNames);

        serviceRegistry.with(ModuleManager.class).lifetime(Lifetime.Singleton).use(() -> moduleManager);

        EntitySystemSetupUtil.addReflectionBasedLibraries(serviceRegistry);

        return moduleManager;
    }

    @Override
    protected void setupPathManager() throws IOException {
        Path tempHome = Files.createTempDirectory("terasology-env");
        tempHome.toFile().deleteOnExit();
        PathManager.getInstance().useOverrideHomePath(tempHome);
    }

    @Override
    protected void setupComponentManager(ServiceRegistry serviceRegistry) {
        serviceRegistry.with(ComponentSystemManager.class).lifetime(Lifetime.Singleton).use(ComponentSystemManager.class);
    }

    @Override
    protected void initComponentManager(Context context) {
        context.get(ComponentSystemManager.class).initialise();
    }

    @Override
    protected void setupWorldProvider(ServiceRegistry serviceRegistry) {
        WorldProvider worldProvider = mock(WorldProvider.class);
        when(worldProvider.getWorldInfo()).thenReturn(new WorldInfo());
        when(worldProvider.getTime()).thenReturn(WORLD_TIME);
        serviceRegistry.with(WorldProvider.class).lifetime(Lifetime.Singleton).use(() -> worldProvider);
    }

    @Override
    protected void setupCelestialSystem(ServiceRegistry serviceRegistry) {
        serviceRegistry.with(CelestialModel.class).lifetime(Lifetime.Singleton).use(BasicCelestialModel.class);
        serviceRegistry.with(CelestialSystem.class).lifetime(Lifetime.Singleton).use(DefaultCelestialSystem.class);
    }

    @Override
    protected void loadPrefabs() {
        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
    }

    @Override
    public void close() throws RuntimeException {
        // it would be nice, if elements in the context implemented (Auto)Closeable
        // TODO: It appears that Gestalt-8 DI does close AutoClosable objects in the context on exit?

        // The StorageManager creates a thread pool (through TaskMaster)
        // which isn't closed automatically
        StorageManager storageManager = context.get(StorageManager.class);
        if (storageManager != null) {
            storageManager.finishSavingAndShutdown();
        }

        super.close();
    }

}

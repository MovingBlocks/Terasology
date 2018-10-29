/*
 * Copyright 2018 MovingBlocks
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
package org.terasology;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.audio.nullAudio.NullSound;
import org.terasology.audio.nullAudio.NullStreamingSound;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.headless.assets.HeadlessMaterial;
import org.terasology.engine.subsystem.headless.assets.HeadlessMesh;
import org.terasology.engine.subsystem.headless.assets.HeadlessShader;
import org.terasology.engine.subsystem.headless.assets.HeadlessSkeletalMesh;
import org.terasology.engine.subsystem.headless.assets.HeadlessTexture;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.ReadWriteStorageManager;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplaySerializer;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.rendering.assets.atlas.Atlas;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontImpl;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.PNGTextureFormat;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.BlockFamilyLibrary;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeImpl;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.block.tiles.BlockTile;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.block.tiles.WorldAtlas;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.sun.BasicCelestialModel;
import org.terasology.world.sun.CelestialSystem;
import org.terasology.world.sun.DefaultCelestialSystem;
import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Setup a headless ( = no graphics ) environment.
 * Based on TerasologyTestingEnvironment code.
 *
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
    protected void setupStorageManager() throws IOException {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        EngineEntityManager engineEntityManager = context.get(EngineEntityManager.class);
        BlockManager blockManager = context.get(BlockManager.class);
        BiomeManager biomeManager = context.get(BiomeManager.class);
        RecordAndReplaySerializer recordAndReplaySerializer = context.get(RecordAndReplaySerializer.class);
        Path savePath = PathManager.getInstance().getSavePath("world1");
        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);

        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        context.put(BlockFamilyLibrary.class, new BlockFamilyLibrary(environment,context));
        
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);

        context.put(StorageManager.class, new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(),
                engineEntityManager, blockManager, biomeManager, extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus));
    }

    @Override
    protected void setupNetwork() {
        EngineTime mockTime = mock(EngineTime.class);
        context.put(Time.class, mockTime);
        NetworkSystem networkSystem = new NetworkSystemImpl(mockTime, getContext());
        context.put(NetworkSystem.class, networkSystem);
    }

    @Override
    protected void setupEntitySystem() {
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
    }

    @Override
    protected void setupCollisionManager() {
        CollisionGroupManager collisionGroupManager = new CollisionGroupManager();
        context.put(CollisionGroupManager.class, collisionGroupManager);
        context.get(TypeSerializationLibrary.class).addTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler(collisionGroupManager));
    }

    @Override
    protected void setupBlockManager(AssetManager assetManager) {
        WorldAtlas worldAtlas = new NullWorldAtlas();
        BlockManagerImpl blockManager = new BlockManagerImpl(worldAtlas, assetManager);
        context.put(BlockManager.class, blockManager);
        TypeSerializationLibrary typeSerializationLibrary = context.get(TypeSerializationLibrary.class);
        typeSerializationLibrary.addTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler(blockManager));
        typeSerializationLibrary.addTypeHandler(Block.class, new BlockTypeHandler(blockManager));
    }
    
    @Override
    protected void setupExtraDataManager(Context context) {
        context.put(ExtraBlockDataManager.class, new ExtraBlockDataManager(context));
    }

    @Override
    protected AssetManager setupEmptyAssetManager() {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.switchEnvironment(context.get(ModuleManager.class).getEnvironment());

        context.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected AssetManager setupAssetManager() {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.registerCoreAssetType(Prefab.class,
                PojoPrefab::new, false, "prefabs");
        assetTypeManager.registerCoreAssetType(BlockShape.class,
                BlockShapeImpl::new, "shapes");
        assetTypeManager.registerCoreAssetType(BlockSounds.class,
                BlockSounds::new, "blockSounds");
        assetTypeManager.registerCoreAssetType(BlockTile.class,
                BlockTile::new, "blockTiles");
        assetTypeManager.registerCoreAssetType(BlockFamilyDefinition.class,
                BlockFamilyDefinition::new, "blocks");

        assetTypeManager.registerCoreAssetType(StaticSound.class, NullSound::new, "sounds");
        assetTypeManager.registerCoreAssetType(StreamingSound.class, NullStreamingSound::new, "music");

        assetTypeManager.registerCoreFormat(BlockFamilyDefinition.class,
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager()));

        assetTypeManager.registerCoreAssetType(UISkin.class,
                UISkin::new, "skins");
        assetTypeManager.registerCoreAssetType(BehaviorTree.class,
                BehaviorTree::new, false, "behaviors");
        assetTypeManager.registerCoreAssetType(UIElement.class,
                UIElement::new, "ui");
        assetTypeManager.registerCoreAssetType(Font.class,
                FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class,
                HeadlessTexture::new, "textures", "fonts");
        assetTypeManager.registerCoreFormat(Texture.class,
                new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> path.getName(2).toString().equals("textures")));
        assetTypeManager.registerCoreFormat(Texture.class,
                new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getName(2).toString().equals("fonts")));

        assetTypeManager.registerCoreAssetType(Shader.class,
                HeadlessShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class,
                HeadlessMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class,
                HeadlessMesh::new, "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class,
                HeadlessSkeletalMesh::new, "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class,
                MeshAnimationImpl::new, "animations");

        assetTypeManager.registerCoreAssetType(Atlas.class,
                Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(Subtexture.class,
                Subtexture::new);

        assetTypeManager.switchEnvironment(context.get(ModuleManager.class).getEnvironment());

        context.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected void setupAudio() {
        NullAudioManager audioManager = new NullAudioManager();
        context.put(AudioManager.class, audioManager);
    }

    @Override
    protected void setupConfig() {
        Config config = new Config(context);
        config.loadDefaults();
        context.put(Config.class, config);
    }

    @Override
    protected void setupModuleManager(Set<Name> moduleNames) throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleRegistry registry = moduleManager.getRegistry();

        DependencyResolver resolver = new DependencyResolver(registry);
        ResolutionResult result = resolver.resolve(moduleNames);

        if (result.isSuccess()) {
            ModuleEnvironment modEnv = moduleManager.loadEnvironment(result.getModules(), true);
            logger.debug("Loaded modules: " + modEnv.getModuleIdsOrderedByDependencies());
        } else {
            logger.error("Could not resolve module dependencies for " + moduleNames);
        }

        context.put(ModuleManager.class, moduleManager);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
    }

    /**
     * @throws IOException ShrinkWrap errors
     */
    @Override
    protected void setupPathManager() throws IOException {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));
    }

    @Override
    protected void setupComponentManager() {
        ComponentSystemManager componentSystemManager = new ComponentSystemManager(context);
        componentSystemManager.initialise();
        context.put(ComponentSystemManager.class, componentSystemManager);
    }

    @Override
    protected void setupWorldProvider() {
        WorldProvider worldProvider = mock(WorldProvider.class);
        when(worldProvider.getWorldInfo()).thenReturn(new WorldInfo());
        when(worldProvider.getTime()).thenReturn(WORLD_TIME);
        context.put(WorldProvider.class, worldProvider);
    }

    @Override
    protected void setupCelestialSystem() {
        DefaultCelestialSystem celestialSystem = new DefaultCelestialSystem(new BasicCelestialModel(), context);
        context.put(CelestialSystem.class, celestialSystem);
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
    public void close() throws Exception {
        // it would be nice, if elements in the context implemented (Auto)Closeable

        // The StorageManager creates a thread pool (through TaskMaster)
        // which isn't closed automatically
        StorageManager storageManager = context.get(StorageManager.class);
        if (storageManager != null) {
            storageManager.finishSavingAndShutdown();
        }


        super.close();
    }

}

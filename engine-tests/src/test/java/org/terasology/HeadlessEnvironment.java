/*
 * Copyright 2013 MovingBlocks
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

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.security.CodeSource;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.audio.AudioManager;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.engine.module.ModuleSecurityManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.StorageManagerInternal;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinData;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.AttachedToSurfaceFamilyFactory;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.HorizontalBlockFamilyFactory;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.NullWorldAtlas;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeData;
import org.terasology.world.block.shapes.BlockShapeImpl;

/**
 * Setup a headless ( = no graphics ) environment.
 * Based on TerasologyTestingEnvironment code.
 * @author Martin Steiger
 */
public class HeadlessEnvironment extends Environment {

    /**
     * Setup a headless ( = no graphics ) environment
     */
    public HeadlessEnvironment() {
        super();
    }
    
    @Override
    protected void setupStorageManager() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        EngineEntityManager engineEntityManager = CoreRegistry.get(EngineEntityManager.class);

        CoreRegistry.put(StorageManager.class, new StorageManagerInternal(moduleManager, engineEntityManager));
    }

    @Override
    protected void setupNetwork() {
        EngineTime mockTime = mock(EngineTime.class);
        CoreRegistry.put(Time.class, mockTime);
        NetworkSystem networkSystem = new NetworkSystemImpl(mockTime);
        CoreRegistry.put(NetworkSystem.class, networkSystem);
    }
    
    @Override
    protected void setupEntitySystem() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        
        EntitySystemBuilder builder = new EntitySystemBuilder();
        EngineEntityManager engineEntityManager = builder.build(moduleManager, networkSystem, new ReflectionReflectFactory());

        CoreRegistry.put(EngineEntityManager.class, engineEntityManager);
    }

    @Override
    protected void setupCollisionManager() {
        CollisionGroupManager collisionGroupManager = new CollisionGroupManager();
        CoreRegistry.put(CollisionGroupManager.class, collisionGroupManager);
    }

    @Override
    protected void setupBlockManager() {
        DefaultBlockFamilyFactoryRegistry blockFamilyFactoryRegistry = new DefaultBlockFamilyFactoryRegistry();
        blockFamilyFactoryRegistry.setBlockFamilyFactory("horizontal", new HorizontalBlockFamilyFactory());
        blockFamilyFactoryRegistry.setBlockFamilyFactory("alignToSurface", new AttachedToSurfaceFamilyFactory());
        WorldAtlas worldAtlas = new NullWorldAtlas();
        BlockManagerImpl blockManager = new BlockManagerImpl(worldAtlas, blockFamilyFactoryRegistry);
        CoreRegistry.put(BlockManager.class, blockManager);
    }

    @Override
    protected void setupEmptyAssetManager() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        AssetManager assetManager = new AssetManager(moduleManager);
        
        // mock an empy asset factory for all asset types
        for (AssetType type : AssetType.values()) {
            assetManager.setAssetFactory(type, mock(AssetFactory.class));
        }

        CoreRegistry.put(AssetManager.class, assetManager);
    }
    
    @Override
    protected void setupAssetManager() {
        setupEmptyAssetManager();
        
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        AudioManager audioManager = CoreRegistry.get(AudioManager.class);
        AssetType.registerAssetTypes(assetManager);

        CodeSource tsCodeSource = TerasologyEngine.class.getProtectionDomain().getCodeSource();
        assetManager.addAssetSource(new ClasspathSource(TerasologyConstants.ENGINE_MODULE, tsCodeSource,
                TerasologyConstants.ASSETS_SUBDIRECTORY, TerasologyConstants.OVERRIDES_SUBDIRECTORY));
        
        CodeSource thisCodeSource = HeadlessEnvironment.class.getProtectionDomain().getCodeSource();
        assetManager.addAssetSource(new ClasspathSource("unittest", thisCodeSource, TerasologyConstants.ASSETS_SUBDIRECTORY,
                TerasologyConstants.OVERRIDES_SUBDIRECTORY));


        assetManager.setAssetFactory(AssetType.PREFAB, new AssetFactory<PrefabData, Prefab>() {

            @Override
            public Prefab buildAsset(AssetUri uri, PrefabData data) {
                return new PojoPrefab(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.SHAPE, new AssetFactory<BlockShapeData, BlockShape>() {

            @Override
            public BlockShape buildAsset(AssetUri uri, BlockShapeData data) {
                return new BlockShapeImpl(uri, data);
            }
        });

        assetManager.setAssetFactory(AssetType.UI_SKIN, new AssetFactory<UISkinData, UISkin>() {
            @Override
            public UISkin buildAsset(AssetUri uri, UISkinData data) {
                return new UISkin(uri, data);
            }
        });

        assetManager.setAssetFactory(AssetType.SOUND, audioManager.getStaticSoundFactory());
        assetManager.setAssetFactory(AssetType.MUSIC, audioManager.getStreamingSoundFactory());
    }        

    @Override
    protected void setupAudio() {
        NullAudioManager audioManager = new NullAudioManager();
        CoreRegistry.put(AudioManager.class, audioManager);
    }

    @Override
    protected void setupConfig() {
        Config config = new Config();
        CoreRegistry.put(Config.class, config);
    }

    @Override
    protected void setupModuleManager() {
        ModuleManagerImpl moduleManager = new ModuleManagerImpl(new ModuleSecurityManager());
        CoreRegistry.put(ModuleManager.class, moduleManager);
    }

    /**
     * @throws IOException ShrinkWrap errors
     */
    @Override
    protected void setupPathManager() throws IOException {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));
        PathManager.getInstance().setCurrentSaveTitle("world1");
    }
    
    @Override
    protected void setupComponentManager() {
        ComponentSystemManager componentSystemManager = new ComponentSystemManager();
        componentSystemManager.initialise();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
    }
    
    @Override
    protected void loadPrefabs() {
        
        LoadPrefabs prefabLoadStep = new LoadPrefabs();

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
    }
    
    @Override
    protected void activateAllModules() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        
        // activate all modules
        for (Module m : moduleManager.getModules()) {
            moduleManager.enableModule(m);
        }

        moduleManager.applyActiveModules();
        assetManager.clear();
        assetManager.applyOverrides();        
    }

    @Override
    public void close() throws Exception {
        // it would be nice, if elements in the CoreRegistry implemented (Auto)Closeable
        
        // The StorageManager creates a thread pool (through TaskMaster)
        // which isn't closed automatically
        StorageManager storageManager = CoreRegistry.get(StorageManager.class);
        if (storageManager != null) {
            storageManager.shutdown();
        }
        
        CoreRegistry.clear();
        
        super.close();
    }

}

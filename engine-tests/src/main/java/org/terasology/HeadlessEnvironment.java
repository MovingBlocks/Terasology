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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.audio.AudioManager;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.ReadWriteStorageManager;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.AttachedToSurfaceFamilyFactory;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.HorizontalBlockFamilyFactory;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.block.tiles.WorldAtlas;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * Setup a headless ( = no graphics ) environment.
 * Based on TerasologyTestingEnvironment code.
 *
 * @author Martin Steiger
 */
public class HeadlessEnvironment extends Environment {

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
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        EngineEntityManager engineEntityManager = CoreRegistry.get(EngineEntityManager.class);
        Path savePath = PathManager.getInstance().getSavePath("world1");

        CoreRegistry.put(StorageManager.class, new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(), engineEntityManager));
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
        EngineEntityManager engineEntityManager = builder.build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());

        CoreRegistry.put(EngineEntityManager.class, engineEntityManager);
    }

    @Override
    protected void setupCollisionManager() {
        CollisionGroupManager collisionGroupManager = new CollisionGroupManager();
        CoreRegistry.put(CollisionGroupManager.class, collisionGroupManager);
    }

    @Override
    protected void setupBlockManager(AssetManager assetManager) {
        DefaultBlockFamilyFactoryRegistry blockFamilyFactoryRegistry = new DefaultBlockFamilyFactoryRegistry();
        blockFamilyFactoryRegistry.setBlockFamilyFactory("horizontal", new HorizontalBlockFamilyFactory());
        blockFamilyFactoryRegistry.setBlockFamilyFactory("alignToSurface", new AttachedToSurfaceFamilyFactory());
        WorldAtlas worldAtlas = new NullWorldAtlas();
        BlockManagerImpl blockManager = new BlockManagerImpl(worldAtlas, assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);
    }

    @Override
    protected AssetManager setupEmptyAssetManager() {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.switchEnvironment(CoreRegistry.get(ModuleManager.class).getEnvironment());

        CoreRegistry.put(AssetManager.class, assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected AssetManager setupAssetManager() {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.switchEnvironment(CoreRegistry.get(ModuleManager.class).getEnvironment());

        CoreRegistry.put(AssetManager.class, assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
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
    public void close() throws Exception {
        // it would be nice, if elements in the CoreRegistry implemented (Auto)Closeable

        // The StorageManager creates a thread pool (through TaskMaster)
        // which isn't closed automatically
        StorageManager storageManager = CoreRegistry.get(StorageManager.class);
        if (storageManager != null) {
            storageManager.finishSavingAndShutdown();
        }

        CoreRegistry.clear();

        super.close();
    }

}

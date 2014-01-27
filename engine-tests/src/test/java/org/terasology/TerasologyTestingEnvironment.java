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

import java.nio.file.FileSystem;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.audio.AudioManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.StorageManagerInternal;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;

/**
 * A base class for unit test classes to inherit to run in a Terasology environment - with LWJGL set up and so forth
 *
 * @author Immortius
 */
public abstract class TerasologyTestingEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyTestingEnvironment.class);

    private static boolean setup;

    private static BlockManager blockManager;
    private static Config config;
    private static AudioManager audioManager;
    private static CollisionGroupManager collisionGroupManager;
    private static ModuleManager moduleManager;
    private static AssetManager assetManager;

    private static DisplayEnvironment env;

    private EngineEntityManager engineEntityManager;
    private ComponentSystemManager componentSystemManager;
    private EngineTime mockTime;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));

        if (!setup) {
            setup = true;

            env = new DisplayEnvironment();
            assetManager = CoreRegistry.get(AssetManager.class);
            blockManager = CoreRegistry.get(BlockManager.class);
            config = CoreRegistry.get(Config.class);
            audioManager = CoreRegistry.get(AudioManager.class);
            collisionGroupManager = CoreRegistry.get(CollisionGroupManager.class);
            moduleManager = CoreRegistry.get(ModuleManager.class);

        } else {
            CoreRegistry.put(AssetManager.class, assetManager);
            CoreRegistry.put(BlockManager.class, blockManager);
            CoreRegistry.put(Config.class, config);
            CoreRegistry.put(AudioManager.class, audioManager);
            CoreRegistry.put(CollisionGroupManager.class, collisionGroupManager);
            CoreRegistry.put(ModuleManager.class, moduleManager);
        }
        PathManager.getInstance().setCurrentSaveTitle("world1");
    }

    @Before
    public void setup() throws Exception {
        CoreRegistry.put(ModuleManager.class, moduleManager);

        mockTime = mock(EngineTime.class);
        CoreRegistry.put(Time.class, mockTime);
        NetworkSystemImpl networkSystem = new NetworkSystemImpl(mockTime);
        CoreRegistry.put(NetworkSystem.class, networkSystem);
        engineEntityManager = new EntitySystemBuilder().build(CoreRegistry.get(ModuleManager.class), networkSystem, new ReflectionReflectFactory());
        CoreRegistry.put(StorageManager.class, new StorageManagerInternal(moduleManager, engineEntityManager));

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        LoadPrefabs prefabLoadStep = new LoadPrefabs();

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
        CoreRegistry.get(ComponentSystemManager.class).initialise();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        CoreRegistry.clear();
        env.close();
    }


    public EngineEntityManager getEntityManager() {
        return engineEntityManager;
    }
}

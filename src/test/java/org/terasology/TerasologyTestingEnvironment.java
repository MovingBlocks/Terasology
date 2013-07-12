/*
 * Copyright 2013 Moving Blocks
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.sources.ClasspathSource;
import org.terasology.audio.AudioManager;
import org.terasology.audio.nullAudio.NullAudioManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Terasology;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.StorageManagerInternal;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.utilities.NativeHelper;
import org.terasology.world.block.family.AlignToSurfaceFamilyFactory;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.HorizontalBlockFamilyFactory;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;

import java.nio.file.FileSystem;

import static org.mockito.Mockito.mock;

/**
 * A base class for unit test classes to inherit to run in a Terasology environment - with LWJGL set up and so forth
 *
 * @author Immortius
 */
public abstract class TerasologyTestingEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyTestingEnvironment.class);

    private static boolean setup = false;

    private EngineEntityManager engineEntityManager;
    private static BlockManager blockManager;
    private static Config config;
    private static AudioManager audioManager;
    private static CollisionGroupManager collisionGroupManager;
    private static ModManager modManager;
    private static NetworkSystem networkSystem;
    private ComponentSystemManager componentSystemManager;
    private EngineTime mockTime;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));
        if (!setup) {
            setup = true;
            bindLwjgl();

            modManager = new ModManager();
            modManager.applyActiveMods();
            CoreRegistry.put(ModManager.class, modManager);
            AssetType.registerAssetTypes();
            AssetManager.getInstance().addAssetSource(new ClasspathSource(ModManager.ENGINE_PACKAGE, Terasology.class.getProtectionDomain().getCodeSource(), ModManager.ASSETS_SUBDIRECTORY, ModManager.OVERRIDES_SUBDIRECTORY));
            AssetManager.getInstance().addAssetSource(new ClasspathSource("unittest", TerasologyTestingEnvironment.class.getProtectionDomain().getCodeSource(), ModManager.ASSETS_SUBDIRECTORY, ModManager.OVERRIDES_SUBDIRECTORY));

            DefaultBlockFamilyFactoryRegistry blockFamilyFactoryRegistry = new DefaultBlockFamilyFactoryRegistry();
            blockFamilyFactoryRegistry.setBlockFamilyFactory("horizontal", new HorizontalBlockFamilyFactory());
            blockFamilyFactoryRegistry.setBlockFamilyFactory("alignToSurface", new AlignToSurfaceFamilyFactory());
            blockManager = new BlockManagerImpl(blockFamilyFactoryRegistry);
            CoreRegistry.put(BlockManager.class, blockManager);

            config = new Config();
            CoreRegistry.put(Config.class, config);

            audioManager = new NullAudioManager();

            CoreRegistry.put(AudioManager.class, audioManager);

            Display.setDisplayMode(new DisplayMode(0, 0));
            Display.create(CoreRegistry.get(Config.class).getRendering().getPixelFormat());

            collisionGroupManager = new CollisionGroupManager();
            CoreRegistry.put(CollisionGroupManager.class, collisionGroupManager);
        } else {
            CoreRegistry.put(BlockManager.class, blockManager);
            CoreRegistry.put(Config.class, config);
            CoreRegistry.put(AudioManager.class, audioManager);
            CoreRegistry.put(CollisionGroupManager.class, collisionGroupManager);
            CoreRegistry.put(ModManager.class, modManager);
        }
        PathManager.getInstance().setCurrentSaveTitle("world1");
    }

    @Before
    public void setup() throws Exception {
        mockTime = mock(EngineTime.class);
        CoreRegistry.put(Time.class, mockTime);
        networkSystem = new NetworkSystemImpl(mockTime);
        CoreRegistry.put(NetworkSystem.class, networkSystem);
        engineEntityManager = new EntitySystemBuilder().build(CoreRegistry.get(ModManager.class), networkSystem);
        CoreRegistry.put(StorageManager.class, new StorageManagerInternal(engineEntityManager));

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        LoadPrefabs prefabLoadStep = new LoadPrefabs();
        prefabLoadStep.begin();
        while (!prefabLoadStep.step()) ;
        CoreRegistry.get(ComponentSystemManager.class).initialise();
    }

    public EngineEntityManager getEntityManager() {
        return engineEntityManager;
    }

    public static void bindLwjgl() throws LWJGLException {
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_MACOSX:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("macosx"));
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("linux"));
                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("openal64");
                } else {
                    System.loadLibrary("openal");
                }
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("windows"));

                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("OpenAL64");
                } else {
                    System.loadLibrary("OpenAL32");
                }
                break;
            default:
                logger.error("Unsupported operating system: {}", LWJGLUtil.getPlatformName());
                System.exit(1);
        }
    }


}

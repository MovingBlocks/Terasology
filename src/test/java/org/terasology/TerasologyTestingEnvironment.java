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

import org.junit.Before;
import org.junit.BeforeClass;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.mod.ModManager;
import org.terasology.utilities.NativeHelper;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerAuthority;

import java.io.File;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * A base class for unit test classes to inherit to run in a Terasology environment - with LWJGL set up and so forth
 *
 * @author Immortius
 */
public abstract class TerasologyTestingEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyTestingEnvironment.class);

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        bindLwjgl();
        CoreRegistry.put(BlockManager.class, new BlockManagerAuthority());
    }

    @Before
    public void setup() throws Exception {
        setupConfig();
        new EntitySystemBuilder().build(new ModManager());
    }

    public static void setupConfig() {
        Config config = new Config();
        CoreRegistry.put(Config.class, config);
    }

    public static void bindLwjgl() throws LWJGLException {
        PathManager.getInstance().determineRootPath(true);
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_MACOSX:
                NativeHelper.addLibraryPath(new File(PathManager.getInstance().getDataPath(), "natives/macosx"));
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                NativeHelper.addLibraryPath(new File(PathManager.getInstance().getDataPath(), "natives/linux"));
                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("openal64");
                } else {
                    System.loadLibrary("openal");
                }
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                NativeHelper.addLibraryPath(new File(PathManager.getInstance().getDataPath(), "natives/windows"));

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

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

package org.terasology.utilities;

import com.google.common.collect.Lists;
import org.lwjgl.LWJGLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.rendering.oculusVr.OculusVrHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Immortius
 */
public final class NativeHelper {

    private static final Logger logger = LoggerFactory.getLogger(NativeHelper.class);

    private NativeHelper() {
    }

    public static void addLibraryPath(Path libPath) {
        try {
            String envPath = System.getProperty("java.library.path");
            if (envPath == null || envPath.isEmpty()) {
                System.setProperty("java.library.path", libPath.toAbsolutePath().toString());
            } else {
                System.setProperty("java.library.path", libPath.toAbsolutePath().toString() + File.pathSeparator + envPath);
            }

            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            List<String> paths = Lists.newArrayList((String[]) usrPathsField.get(null));

            if (paths.contains(libPath.toAbsolutePath().toString())) {
                return;
            }

            paths.add(0, libPath.toAbsolutePath().toString()); // Add to beginning, to override system libraries

            usrPathsField.set(null, paths.toArray(new String[paths.size()]));
        } catch (Exception e) {
            logger.error("Couldn't link static libraries. ", e);
            System.exit(1);
        }
    }

    /**
     * Used on initializing the game environment, either for playing or for running unit tests
     */
    public static void initNativeLibs() {
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_MACOSX:
                addLibraryPath(PathManager.getInstance().getNativesPath().resolve("macosx"));
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                addLibraryPath(PathManager.getInstance().getNativesPath().resolve("linux"));
                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("openal64");
                } else {
                    System.loadLibrary("openal");
                }
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                addLibraryPath(PathManager.getInstance().getNativesPath().resolve("windows"));

                if (System.getProperty("os.arch").contains("64")) {
                    System.loadLibrary("OpenAL64");
                } else {
                    System.loadLibrary("OpenAL32");
                }
                try {
                    OculusVrHelper.loadNatives();
                } catch (UnsatisfiedLinkError e) {
                    logger.warn("Could not load optional TeraOVR native libraries - Oculus support disabled");
                }
                break;
            default:
                logger.error("Unsupported operating system: {}", LWJGLUtil.getPlatformName());
                System.exit(1);
        }
    }
}

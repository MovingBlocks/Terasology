/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities;

import org.lwjgl.LWJGLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.rendering.oculusVr.OculusVrHelper;

/**
 * Helper class to have LWJGL loading logic in a central spot
 *
 */
public final class LWJGLHelper {

    private static final Logger logger = LoggerFactory.getLogger(LWJGLHelper.class);

    private LWJGLHelper() {
    }

    /**
     * Used on initializing the game environment, either for playing or for running unit tests
     */
    public static void initNativeLibs() {
        initLibraryPaths();

        try {
            initOculus();
        } catch (UnsatisfiedLinkError e) {
            logger.warn("Could not load optional TeraOVR native libraries - Oculus support disabled");
        }
    }

    private static void initLibraryPaths() {
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_MACOSX:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("macosx"));
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("linux"));
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                NativeHelper.addLibraryPath(PathManager.getInstance().getNativesPath().resolve("windows"));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operating system: " + LWJGLUtil.getPlatformName());
        }
    }

    private static void initOculus() {
        if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS) {
            OculusVrHelper.loadNatives();
        }
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import org.lwjgl.LWJGLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.paths.PathManager;

import java.nio.file.Path;

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
    }

    private static void initLibraryPaths() {
        final Path path;
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_MACOSX:
                path = PathManager.getInstance().getNativesPath().resolve("macosx");
                break;
            case LWJGLUtil.PLATFORM_LINUX:
                path = PathManager.getInstance().getNativesPath().resolve("linux");
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                path = PathManager.getInstance().getNativesPath().resolve("windows");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operating system: " + LWJGLUtil.getPlatformName());
        }

        final String natives = path.toAbsolutePath().toString();
        System.setProperty("org.lwjgl.librarypath", natives);
        System.setProperty("net.java.games.input.librarypath", natives);  // libjinput
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import java.nio.file.FileSystems;

/**
 * Utility for determinate OperationSystem(aka Platform)
 * Used instead {@link org.lwjgl.system.Platform}, because it invoke static of {@link org.lwjgl.system.Configuration} and load configs to static.
 * It stores System.getProperty and make writing lwjgl's path  - impossible.
 */
public enum OS {
    WINDOWS,
    MACOSX,
    LINUX;

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final String ARCHITECTURE = System.getProperty("os.arch");
    public static final String USER_DIRECTORY = System.getProperty("user.dir");
    public static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().contains("win");
    public static final boolean IS_MACOSX =  OS_NAME.toLowerCase().contains("mac");
    public static final boolean IS_LINUX =  OS_NAME.toLowerCase().contains("linux") | OS_NAME.toLowerCase().contains("freebsd") || OS_NAME.toLowerCase().contains("sunos") || OS_NAME.toLowerCase().contains("unix");
    public static final boolean IS_64 = ARCHITECTURE.endsWith("64");

    public static OS get() {
        String osName = System.getProperty("os.name");
        if (IS_WINDOWS) {
            return WINDOWS;
        } else if (IS_LINUX) {
            return LINUX;
        } else if (IS_MACOSX) {
            return MACOSX;
        } else {
            throw new IllegalStateException("Unknown platform: " + osName);
        }
    }
}

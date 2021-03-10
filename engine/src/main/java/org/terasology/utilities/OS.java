// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

/**
 * Utility for determinate OperationSystem(aka Platform)
 * Used instead {@link org.lwjgl.system.Platform}, because it invoke static of {@link org.lwjgl.system.Configuration} and load configs to static.
 * It stores System.getProperty and make writing lwjgl's path  - impossible.
 */
public enum OS {
    WINDOWS,
    MACOSX,
    LINUX;


    public static OS get() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            return WINDOWS;
        } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            return LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            return MACOSX;
        } else {
            throw new IllegalStateException("Unknown platform: " + osName);
        }
    }
}

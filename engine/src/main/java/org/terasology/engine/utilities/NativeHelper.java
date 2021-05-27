// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NativeHelper {

    private static final Logger logger = LoggerFactory.getLogger(NativeHelper.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String ARCHITECTURE = System.getProperty("os.arch");
    private static final String USER_DIRECTORY = System.getProperty("user.dir");

    private NativeHelper() {
    }


    public static boolean isWin64() {

        return (OS.contains("win") && ARCHITECTURE.contains("64"));

    }

    public static boolean isWin32() {

        return (OS.contains("win") && ARCHITECTURE.contains("32"));

    }

    public static boolean isWindows() {
        return isWin32() || isWin64();
    }

    public static boolean isLinux64() {

        return (OS.contains("linux") && ARCHITECTURE.contains("64"));

    }

    public static boolean isLinux32() {

        return (OS.contains("linux") && ARCHITECTURE.contains("32"));

    }

    public static boolean isMac() {

        return (OS.contains("mac"));

    }

    public static String getOsString() {
        if (isWin64()) {
            return "win32-x86-64";
        } else if (isWin32()) {
            return "win32-x86";
        } else if (isLinux64()) {
            return "linux-x86-64";
        } else if (isLinux32()) {
            return "linux-x86";
        } else if (isMac()) {
            return "darwin";
        }
        return "unknown";
    }

    public static String getOpenVRLibPath() {
        logger.debug("OS string" + OS);
        if (isWindows()) {
            return USER_DIRECTORY + "\\openvr_natives\\" + getOsString();
        }
        return USER_DIRECTORY + "/openvr_natives/" + getOsString();
    }
}

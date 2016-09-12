package org.terasology.rendering.openvrprovider;

/* Mainly used to locate the OpenVR library on different platforms. */
public class OSValidator {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String ARCHITECTURE = System.getProperty("os.arch");
    private static final String USER_DIRECTORY = System.getProperty("user.dir");

    public static boolean isWin64() {

        return (OS.contains("win") && ARCHITECTURE.contains("64"));

    }

    public static boolean isWin32() {

        return (OS.contains("win") && ARCHITECTURE.contains("32"));

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

    public static String getLibPath() {
        if (getOsString().contains("win")) {
            return USER_DIRECTORY + "\\openvr_natives\\" + getOsString();
        }
        return USER_DIRECTORY + "/openvr_natives/" + getOsString();
    }

}

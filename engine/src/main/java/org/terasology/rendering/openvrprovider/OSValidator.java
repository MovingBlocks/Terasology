package org.terasology.rendering.openvrprovider;

/* Mainly used to locate the OpenVR library on different platforms. */
public class OSValidator {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String arch = System.getProperty("os.arch");
    private static final String userDir = System.getProperty("user.dir");

    public static boolean isWin64() {

        return (OS.indexOf("win") >= 0 && arch.contains("64"));

    }

    public static boolean isWin32() {

        return (OS.indexOf("win") >= 0 && arch.contains("32"));

    }

    public static boolean isLinux64() {

        return (OS.indexOf("linux") >= 0 && arch.contains("64"));

    }

    public static boolean isLinux32() {

        return (OS.indexOf("linux") >= 0 && arch.contains("32"));

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static String getOsString() {
        if (isWin64())
            return new String("win32-x86-64");
        else if (isWin32())
            return new String("win32-x86");
        else if (isLinux64())
            return new String("linux-x86-64");
        else if (isLinux32())
            return new String("linux-x86");
        else if (isMac())
            return new String("darwin");
        return new String("unknown");
    }

    public static String getLibPath() {
        if (getOsString().contains("win"))
            return userDir + "\\openvr_natives\\" + getOsString();
        return userDir + "/openvr_natives/" + getOsString();
    }

}

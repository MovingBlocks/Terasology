// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public final class NonNativeJVMDetector {

    /**
     * This is <code>true</code> if the engine is running in a 32-bit JVM on a 64-bit system.
     */
    public static final boolean JVM_ARCH_IS_NONNATIVE = jvmIsNotNative();

    private NonNativeJVMDetector() {
    }

    private static boolean jvmIsNotNative() {
        if (System.getProperty("os.name").contains("Windows")) {
            //Windows sets the PROCESSOR_ARCHITEW6432 env variable for processes running under WOW64
            //Source: https://blogs.msdn.microsoft.com/david.wang/2006/03/27/howto-detect-process-bitness/
            return System.getenv("PROCESSOR_ARCHITEW6432") != null;
        } else {
            //Assuming we are on a POSIX-compliant system if we aren't on a Windows system
            try {
                return !jvmIs64() && posixSystemIs64();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private static boolean posixSystemIs64() throws IOException, InterruptedException {
        Process unameProc = new ProcessBuilder("uname", "-m").start();
        unameProc.waitFor();
        try (BufferedReader unameStdout = new BufferedReader(new InputStreamReader(unameProc.getInputStream()))) {
            return unameStdout.readLine().endsWith("64");
        }
    }

    private static boolean jvmIs64() {
        return System.getProperty("os.arch").endsWith("64"); //match amd64, x86_64, etc
    }
}

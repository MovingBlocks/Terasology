// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import com.sun.jna.platform.unix.LibC;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Monitor process memory usage.
 * <p>
 * This checks process's total memory usage as seen by the operating system.
 * This includes memory not managed by the JVM.
 */
public final class OperatingSystemMemory {
    public static final int PAGE_SIZE = 1 << 12;  // 4 kB on x86 platforms

    private static final Path PROC_STATM = Path.of("/proc/self/statm");

    private OperatingSystemMemory() { }

    public static boolean isAvailable() {
        return OS.IS_LINUX;
    }

    public static long residentSetSize() {
        try {
            return STATM.RESIDENT.inBytes(Files.readString(PROC_STATM));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long dataAndStackSize() {
        try {
            return STATM.DATA.inBytes(Files.readString(PROC_STATM));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long dataAndStackSizeLimit() {
        final LibC.Rlimit dataLimit = new LibC.Rlimit();
        LibC.INSTANCE.getrlimit(LibC.RLIMIT_DATA, dataLimit);
        return dataLimit.rlim_cur;
    }

    /**
     * The fields of /proc/[pid]/statm
     * <p>
     * Note from proc(5):
     * <blockquote><p>
     * Some of these values are inaccurate because of a kernel-internal scalability optimization.
     * If accurate values are required, use /proc/[pid]/smaps or /proc/[pid]/smaps_rollup instead,
     * which are much slower but provide accurate, detailed information.
     * </p></blockquote>
     */
    enum STATM {
        /** total program size */
        SIZE(0),
        /** resident set size */
        RESIDENT(1),
        /** number of resident shared pages */
        SHARED(2),
        /** text (code) */
        TEXT(3),
        /** unused since Linux 2.6 */
        LIB(4),
        /** data + stack */
        DATA(5),
        /** unused since Linux 2.6 */
        DT(6);

        private final short index;

        STATM(int i) {
            index = (short) i;
        }

        public long rawValue(String line) {
            return Long.parseLong(line.split(" ")[index]);
        }

        public long inBytes(String line) {
            return rawValue(line) * PAGE_SIZE;
        }
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.utilities;

import org.slf4j.Logger;
import org.terasology.engine.paths.PathManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Jvm {
    private Jvm() { }

    @SuppressWarnings("CheckStyle")
    static final Pattern gradleCache = Pattern.compile(".*\\Wgradle\\Wcaches?\\b.*?(/.+)?/(.*\\.jar)$");

    public static void logClasspath(Logger aLogger) {
        String interestingGroup = "org.terasology";
        String home = PathManager.getInstance().getInstallPath().toString();
        String indent = "  ";
        int elidedCount = 0;

        for (String pathEntry : System.getProperty("java.class.path").split(System.getProperty("path.separator", ":"))) {
            Matcher asGradleCache = gradleCache.matcher(pathEntry);
            if (asGradleCache.matches()) {
                if (asGradleCache.group(1).contains(interestingGroup)) {
                    aLogger.debug("{}gradle:{}", indent, asGradleCache.group(2));
                } else {
                    elidedCount++;
                }
            } else {
                String place = pathEntry;
                if (pathEntry.startsWith(home)) {
                    place = pathEntry.replace(home, "⌂");
                }
                aLogger.debug("{}{}", indent, place);
            }
        }
        if (elidedCount > 0) {
            aLogger.debug("{}+ {} more jars from external projects in the gradle cache", indent, elidedCount);
        }
    }
}

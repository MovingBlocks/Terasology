// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import org.slf4j.Logger;
import org.terasology.engine.core.paths.PathManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Jvm {
    private Jvm() { }

    @SuppressWarnings("CheckStyle")
    static final Pattern gradleCache = Pattern.compile(".*\\Wgradle\\Wcaches?\\b.*?(/.+)?/(.*\\.jar)$");

    @SuppressWarnings("CheckStyle")
    static final Pattern mavenCache = Pattern.compile(".*/.m2/repo.*/(.+\\.jar)$");

    public static void logClasspath(Logger aLogger) {
        String interestingGroup = "org.terasology";
        String projectRoot = PathManager.getInstance().getInstallPath().toString();
        String userHome = System.getProperty("user.home");
        String indent = "  ";
        int elidedCount = 0;

        for (String pathEntry : System.getProperty("java.class.path").split(System.getProperty("path.separator", ":"))) {
            Matcher asGradleCache = gradleCache.matcher(pathEntry);
            Matcher asMavenCache = mavenCache.matcher(pathEntry);
            if (asGradleCache.matches()) {
                if (asGradleCache.group(1).contains(interestingGroup)) {
                    aLogger.debug("{}gradle:{}", indent, asGradleCache.group(2));
                } else {
                    elidedCount++;
                }
            } else if (asMavenCache.matches()) {
                aLogger.debug("{}maven:{}", indent, asMavenCache.group(1));
            } else {
                String place = pathEntry;
                if (pathEntry.startsWith(projectRoot)) {
                    place = pathEntry.replace(projectRoot, "⌂");
                } else if (pathEntry.startsWith(userHome)) {
                    place = pathEntry.replace(userHome, "~");
                }
                aLogger.debug("{}{}", indent, place);
            }
        }
        if (elidedCount > 0) {
            aLogger.debug("{}+ {} more jars from external projects in the gradle cache", indent, elidedCount);
        }
    }
}

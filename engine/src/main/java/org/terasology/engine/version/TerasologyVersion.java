// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TerasologyVersion {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyVersion.class);

    private static TerasologyVersion instance;

    private static final String VERSION_INFO_FILE = "versionInfo.properties";

    private static final String BUILD_NUMBER = "buildNumber";
    private static final String BUILD_ID = "buildId";
    private static final String BUILD_TAG = "buildTag";
    private static final String BUILD_URL = "buildUrl";
    private static final String JOB_NAME = "jobName";
    private static final String DATE_TIME = "dateTime";
    private static final String DISPLAY_VERSION = "displayVersion";
    private static final String ENGINE_VERSION = "engineVersion";

    private static final String DEFAULT_VALUE = "";

    private final String buildNumber;
    private final String buildId;
    private final String buildTag;
    private final String buildUrl;
    private final String jobName;
    private final String dateTime;
    private final String toString;
    private final String displayVersion;
    private final String engineVersion;

    private TerasologyVersion() {
        final Properties properties = new Properties();
        try (InputStream inStream = this.getClass().getResourceAsStream(VERSION_INFO_FILE)) {
            if (inStream != null) {
                properties.load(inStream);
            }
        } catch (final IOException e) {
            logger.error("Loading {} failed", VERSION_INFO_FILE, e);
        }

        buildNumber = properties.getProperty(BUILD_NUMBER, DEFAULT_VALUE);
        buildId = properties.getProperty(BUILD_ID, DEFAULT_VALUE);
        buildTag = properties.getProperty(BUILD_TAG, DEFAULT_VALUE);
        buildUrl = properties.getProperty(BUILD_URL, DEFAULT_VALUE);
        jobName = properties.getProperty(JOB_NAME, DEFAULT_VALUE);
        dateTime = properties.getProperty(DATE_TIME, DEFAULT_VALUE);
        displayVersion = properties.getProperty(DISPLAY_VERSION, DEFAULT_VALUE);
        engineVersion = properties.getProperty(ENGINE_VERSION, DEFAULT_VALUE);

        final StringBuilder toStringBuilder = new StringBuilder();
        toStringBuilder.append("[");
        toStringBuilder.append(BUILD_NUMBER);
        toStringBuilder.append("=");
        toStringBuilder.append(buildNumber);
        toStringBuilder.append(", ");
        toStringBuilder.append(BUILD_ID);
        toStringBuilder.append("=");
        toStringBuilder.append(buildId);
        toStringBuilder.append(", ");
        toStringBuilder.append(BUILD_TAG);
        toStringBuilder.append("=");
        toStringBuilder.append(buildTag);
        toStringBuilder.append(", ");
        toStringBuilder.append(BUILD_URL);
        toStringBuilder.append("=");
        toStringBuilder.append(buildUrl);
        toStringBuilder.append(", ");
        toStringBuilder.append(JOB_NAME);
        toStringBuilder.append("=");
        toStringBuilder.append(jobName);
        toStringBuilder.append(", ");
        toStringBuilder.append(DATE_TIME);
        toStringBuilder.append("=");
        toStringBuilder.append(dateTime);
        toStringBuilder.append(", ");
        toStringBuilder.append(DISPLAY_VERSION);
        toStringBuilder.append("=");
        toStringBuilder.append(displayVersion);
        toStringBuilder.append(", ");
        toStringBuilder.append(ENGINE_VERSION);
        toStringBuilder.append("=");
        toStringBuilder.append(engineVersion);
        toStringBuilder.append("]");
        toString = toStringBuilder.toString();
    }

    public static TerasologyVersion getInstance() {
        if (instance == null) {
            instance = new TerasologyVersion();
        }
        return instance;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getBuildId() {
        return buildId;
    }

    public String getBuildTag() {
        return buildTag;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public String getJobName() {
        return jobName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    public String getengineVersion() {
        return engineVersion;
    }

    /**
     * Makes a pretty version string for displaying to human users
     * @return prettified version String
     */
    public String getHumanVersion() {
        String humanVersion = "";
        TerasologyVersion ver = getInstance();

        // Game-level release name: Alpha-## until we hit Beta - not engine-specific, but defined here for now
        if (!ver.getDisplayVersion().trim().equals("")) {
            humanVersion = displayVersion.toUpperCase() + " ";
        }

        String formattedDate = "";
        // Expect a date string but ignore time of day
        if (ver.getDateTime().trim().length() > 10) {
            formattedDate += ver.getDateTime().substring(0, 10);
        }

        // Use the job name from Jenkins to determine which flavor we're dealing with (release, dev build, other)
        if (jobName.equals("Terasology/engine/master")) {
            // This is a release, hopefully stable, but who knows ..
            humanVersion += "Release \n(engine v" + engineVersion + ", build " + buildNumber + ", " + formattedDate + ")";
        } else if (jobName.equals("Terasology/engine/develop")) {
            // This is a dev build, so a snapshot for the given release name
            humanVersion += "Preview \n(engine v" + engineVersion + ", dev build " + buildNumber + ", " + formattedDate + ")";
        } else if (!jobName.equals("")) {
            // This is some other actual build that came from Jenkins
            humanVersion += "Special: " + jobName + "\n(engine v" + engineVersion + ", build " + buildNumber + ", " + formattedDate + ")";
        } else {
            // Likely this didn't come from Jenkins at all
            humanVersion += "Custom version - running from source or hand-built";
        }

        return humanVersion;
    }

    @Override
    public String toString() {
        return toString;
    }

}

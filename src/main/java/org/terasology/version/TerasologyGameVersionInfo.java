/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mathias Kalb
 */
public final class TerasologyGameVersionInfo {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyGameVersionInfo.class);

    private static TerasologyGameVersionInfo instance;

    private static final String VERSION_INFO_FILE = "versionInfo.properties";

    private static final String BUILD_NUMBER = "buildNumber";
    private static final String BUILD_ID = "buildId";
    private static final String BUILD_TAG = "buildTag";
    private static final String BUILD_URL = "buildUrl";
    private static final String JOB_NAME = "jobName";
    private static final String GIT_BRANCH = "gitBranch";
    private static final String GIT_COMMIT = "gitCommit";
    private static final String DATE_TIME = "dateTime";
    private static final String DISPLAY_VERSION = "displayVersion";

    private static final String DEFAULT_VALUE = "";

    private final String buildNumber;
    private final String buildId;
    private final String buildTag;
    private final String buildUrl;
    private final String jobName;
    private final String gitBranch;
    private final String gitCommit;
    private final String dateTime;
    private final String displayVersion;
    private final String toString;

    private TerasologyGameVersionInfo() {
        final Properties properties = new Properties();
        final InputStream inStream = this.getClass().getResourceAsStream(VERSION_INFO_FILE);
        if (inStream != null) {
            try {
                properties.load(inStream);
            } catch (final IOException e) {
                logger.error("Loading version info failed!", e);
            } finally {
                // JAVA7 : cleanup
                try {
                    inStream.close();
                } catch (final IOException e) {
                    logger.info("Closing InputStream failed!", e);
                }
            }
        }

        buildNumber = properties.getProperty(BUILD_NUMBER, DEFAULT_VALUE);
        buildId = properties.getProperty(BUILD_ID, DEFAULT_VALUE);
        buildTag = properties.getProperty(BUILD_TAG, DEFAULT_VALUE);
        buildUrl = properties.getProperty(BUILD_URL, DEFAULT_VALUE);
        jobName = properties.getProperty(JOB_NAME, DEFAULT_VALUE);
        gitBranch = properties.getProperty(GIT_BRANCH, DEFAULT_VALUE);
        gitCommit = properties.getProperty(GIT_COMMIT, DEFAULT_VALUE);
        dateTime = properties.getProperty(DATE_TIME, DEFAULT_VALUE);
        displayVersion = properties.getProperty(DISPLAY_VERSION, DEFAULT_VALUE);

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
        toStringBuilder.append(GIT_BRANCH);
        toStringBuilder.append("=");
        toStringBuilder.append(gitBranch);
        toStringBuilder.append(", ");
        toStringBuilder.append(GIT_COMMIT);
        toStringBuilder.append("=");
        toStringBuilder.append(gitCommit);
        toStringBuilder.append(", ");
        toStringBuilder.append(DATE_TIME);
        toStringBuilder.append("=");
        toStringBuilder.append(dateTime);
        toStringBuilder.append(", ");
        toStringBuilder.append(DISPLAY_VERSION);
        toStringBuilder.append("=");
        toStringBuilder.append(displayVersion);
        toStringBuilder.append("]");
        toString = toStringBuilder.toString();
    }

    public static TerasologyGameVersionInfo getInstance() {
        if (instance == null) {
            instance = new TerasologyGameVersionInfo();
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

    public String getGitBranch() {
        return gitBranch;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    @Override
    public String toString() {
        return toString;
    }

}

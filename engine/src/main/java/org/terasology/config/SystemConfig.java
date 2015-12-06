/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.config;

import java.util.Locale;
import java.util.Locale.Category;

/**
 */
public class SystemConfig {
    public static final String SAVED_GAMES_ENABLED_PROPERTY = "org.terasology.savedGamesEnabled";

    private long dayNightLengthInMs;
    private int maxThreads;
    private int maxSecondsBetweenSaves;
    private int maxUnloadedChunksPercentageTillSave;
    private boolean debugEnabled;
    private boolean monitoringEnabled;
    private boolean writeSaveGamesEnabled;
    private String locale;

    public long getDayNightLengthInMs() {
        return dayNightLengthInMs;
    }

    public void setDayNightLengthInMs(long dayNightLengthInMs) {
        this.dayNightLengthInMs = dayNightLengthInMs;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxSecondsBetweenSaves() {
        return maxSecondsBetweenSaves;
    }

    public void setMaxSecondsBetweenSaves(int maxSecondsBetweenSaves) {
        this.maxSecondsBetweenSaves = maxSecondsBetweenSaves;
    }

    public int getMaxUnloadedChunksPercentageTillSave() {
        return maxUnloadedChunksPercentageTillSave;
    }

    public void setMaxUnloadedChunksPercentageTillSave(int maxUnloadedChunksPercentageTillSave) {
        this.maxUnloadedChunksPercentageTillSave = maxUnloadedChunksPercentageTillSave;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void setMonitoringEnabled(boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public boolean isWriteSaveGamesEnabled() {
        String property = System.getProperty(SAVED_GAMES_ENABLED_PROPERTY);
        if (property != null) {
            return Boolean.parseBoolean(property);
        }
        return writeSaveGamesEnabled;
    }

    public void setWriteSaveGamesEnabled(boolean writeSaveGamesEnabled) {
        this.writeSaveGamesEnabled = writeSaveGamesEnabled;
    }

    public Locale getLocale() {
        if (locale == null) {
            setLocale(Locale.getDefault(Category.DISPLAY));
        }
        return Locale.forLanguageTag(locale);
    }

    public void setLocale(Locale locale) {
        this.locale = locale.toLanguageTag();
    }
}

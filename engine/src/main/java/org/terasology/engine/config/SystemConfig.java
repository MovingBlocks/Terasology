// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import java.util.Locale;
import java.util.Locale.Category;

public class SystemConfig {
    public static final String SAVED_GAMES_ENABLED_PROPERTY = "org.terasology.savedGamesEnabled";
    public static final String PERMISSIVE_SECURITY_ENABLED_PROPERTY = "org.terasology.permissiveSecurityEnabled";

    private long dayNightLengthInMs;
    private int maxThreads;
    private int maxSecondsBetweenSaves;
    private int maxUnloadedChunksPercentageTillSave;
    private boolean debugEnabled;
    private boolean monitoringEnabled;
    private boolean writeSaveGamesEnabled;
    private long chunkGenerationFailTimeoutInMs;
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

    public long getChunkGenerationFailTimeoutInMs() {
        return chunkGenerationFailTimeoutInMs;
    }

    public void setChunkGenerationFailTimeoutInMs(long chunkGenerationFailTimeoutInMs) {
        this.chunkGenerationFailTimeoutInMs = chunkGenerationFailTimeoutInMs;
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

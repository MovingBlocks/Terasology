// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.internal;

import org.terasology.engine.core.SimpleUri;

/**
 * Summary information on a world.
 *
 */
public class WorldInfo {

    private String title = "";
    private String customTitle = "";
    private String seed = "";
    private long time;
    private SimpleUri worldGenerator = new SimpleUri();

    public WorldInfo() {
    }

    public WorldInfo(String title, String seed, long time, SimpleUri worldGenerator) {
        this(title, "", seed, time, worldGenerator);
    }

    public WorldInfo(String title, String customTitle, String seed, long time, SimpleUri worldGenerator) {
        if (title != null) {
            this.title = title;
        }
        this.customTitle = customTitle;
        if (seed != null) {
            this.seed = seed;
        }
        if (worldGenerator != null) {
            this.worldGenerator = worldGenerator;
        }
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        if (seed != null) {
            this.seed = seed;
        }
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public SimpleUri getWorldGenerator() {
        return worldGenerator;
    }

    public void setWorldGenerator(SimpleUri worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    @Override
    public String toString() {
        return title;
    }
}

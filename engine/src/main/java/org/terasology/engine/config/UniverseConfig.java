// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import com.google.common.collect.Lists;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.world.internal.WorldInfo;

import java.util.List;

public class UniverseConfig {
    private List<WorldInfo> worlds = Lists.newArrayList();
    private String spawnWorldTitle;
    private String universeSeed;

    public UniverseConfig() {
        worlds.clear();
    }

    public void addWorldManager(WorldInfo worldInfo) {
        if (worldInfo.getTitle().equals(TerasologyConstants.MAIN_WORLD)) {
            worlds.clear();
            this.spawnWorldTitle = worldInfo.getTitle();
        }
        worlds.add(worldInfo);
    }

    public void setSpawnWorldTitle(String targetWorldTitle) {
        spawnWorldTitle = targetWorldTitle;
    }

    public void setUniverseSeed(String seed) {
        universeSeed = seed;
    }

    public String getSpawnWorldTitle() {
        return spawnWorldTitle;
    }

    public List<WorldInfo> getWorlds() {
        return worlds;
    }
}

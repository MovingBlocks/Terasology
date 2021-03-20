/*
 * Copyright 2018 MovingBlocks
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

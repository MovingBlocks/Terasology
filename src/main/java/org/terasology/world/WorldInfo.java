/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world;

import org.terasology.world.generator.WorldGeneratorUri;

/**
 * Summary information on a world.
 *
 * @author Immortius
 */
public class WorldInfo {

    private String title = "";
    private String seed = "";
    private long time = 0;
    private WorldGeneratorUri worldGenerator = new WorldGeneratorUri("engine", "perlin");

    public WorldInfo() {
    }

    public WorldInfo(String title, String seed, long time, WorldGeneratorUri worldGenerator) {
        if (title != null) {
            this.title = title;
        }
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

    public WorldGeneratorUri getWorldGenerator() {
        return worldGenerator;
    }

    public void setWorldGenerator(WorldGeneratorUri worldGenerator) {
        this.worldGenerator = worldGenerator;
    }
}

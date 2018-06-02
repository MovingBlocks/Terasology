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
package org.terasology.rendering.world;

import org.terasology.naming.Name;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.internal.WorldGeneratorInfo;

public class World {

    private Name worldName;
    private WorldGeneratorInfo worldGeneratorInfo;
    private WorldConfigurator worldConfigurator;

    public World(Name worldName, WorldGeneratorInfo worldGeneratorInfo) {
        this.worldName = worldName;
        this.worldGeneratorInfo = worldGeneratorInfo;
    }

    public WorldGeneratorInfo getWorldGeneratorInfo() {
        return this.worldGeneratorInfo;
    }

    public Name getWorldName() {
        return this.worldName;
    }

    public void setWorldConfigurator(WorldConfigurator worldConfigurator) {
        this.worldConfigurator = worldConfigurator;
    }

    public WorldConfigurator getWorldConfigurator() {
        return worldConfigurator;
    }
}

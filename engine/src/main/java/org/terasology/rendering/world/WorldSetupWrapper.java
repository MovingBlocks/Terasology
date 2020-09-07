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

import org.terasology.gestalt.naming.Name;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorInfo;

/**
 * This class only has significance during the create game phase.
 * Every world is an object of this class.
 */
public class WorldSetupWrapper {

    private Name worldName;
    private WorldGeneratorInfo worldGeneratorInfo;
    private WorldConfigurator worldConfigurator;
    private WorldGenerator worldGenerator;

    /**
     * A constructor to initialise a world object.
     * @param worldName The World Name which is displayed in the drop-downs.
     * @param worldGeneratorInfo Contains the {@link WorldGeneratorInfo} object for that world.
     */
    public WorldSetupWrapper(Name worldName, WorldGeneratorInfo worldGeneratorInfo) {
        this.worldName = worldName;
        this.worldGeneratorInfo = worldGeneratorInfo;
    }

    public WorldGeneratorInfo getWorldGeneratorInfo() {
        return this.worldGeneratorInfo;
    }

    public Name getWorldName() {
        return this.worldName;
    }

    public void setWorldName(Name newName) {
        this.worldName = newName;
    }

    public void setWorldConfigurator(WorldConfigurator worldConfigurator) {
        this.worldConfigurator = worldConfigurator;
    }

    public WorldConfigurator getWorldConfigurator() {
        return worldConfigurator;
    }

    public void setWorldGenerator(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }
}

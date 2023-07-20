// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import org.terasology.gestalt.naming.Name;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorInfo;

/**
 * This class only has significance during the create game phase.
 * Every world is an object of this class.
 */
public class WorldSetupWrapper {

    private Name worldName;
    private WorldConfigurator worldConfigurator;
    private WorldGenerator worldGenerator;

    /**
     * A constructor to initialise a world object.
     * @param worldName The World Name which is displayed in the drop-downs.
     * @param worldGenerator Contains the {@link WorldGenerator} object for that world.
     */
    public WorldSetupWrapper(Name worldName, WorldGenerator worldGenerator) {
        this.worldName = worldName;
        this.worldGenerator = worldGenerator;
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

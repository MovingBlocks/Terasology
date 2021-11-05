// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.world.internal.WorldInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * WorldManager is a class which creates the bridge between the world pools and the worlds added to the GameManifest.
 * The class maintains a {@code Map<WorldInfo, EngineEntityPool>} and information about the target world. There are methods
 * to add more pools and worlds to the map and to change the current world.
 */
public class WorldManager {

    private Map<WorldInfo, EngineEntityPool> worldPoolMap = new HashMap<>();
    private WorldInfo currentWorld;

    public WorldManager(WorldInfo currentWorld) {
        this.currentWorld = currentWorld;
    }

    public Map<WorldInfo, EngineEntityPool> getWorldPoolMap() {
        return worldPoolMap;
    }

    public void addWorldPool(WorldInfo world, EngineEntityPool pool) {
        worldPoolMap.put(world, pool);
    }

    public void setCurrentWorld(WorldInfo currentWorld) {
        this.currentWorld = currentWorld;
    }

    public WorldInfo getCurrentWorld() {
        return currentWorld;
    }

    public EngineEntityPool getCurrentWorldPool() {
        return worldPoolMap.get(currentWorld);
    }
}

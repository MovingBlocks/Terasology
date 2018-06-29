///*
// * Copyright 2018 MovingBlocks
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
package org.terasology.entitySystem.entity.internal;

import org.terasology.world.internal.WorldInfo;

import java.util.HashMap;
import java.util.Map;

public class WorldManager {

    private Map<WorldInfo, EngineEntityPool> worldPoolMap = new HashMap<>();
    private WorldInfo currentWorld;

    public WorldManager(WorldInfo currentWorld) {
        this.currentWorld = currentWorld;
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

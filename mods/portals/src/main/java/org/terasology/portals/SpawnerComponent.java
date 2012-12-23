/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.portals;

import org.terasology.entitySystem.Component;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Component that enables an entity to be a spawner of something
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class SpawnerComponent implements Component {

    /** Types of Spawnables this Spawner can spawn */
    public List<String> types = Lists.newArrayList();
    
    public long lastTick=0;
    
    public int timeBetweenSpawns = 5000;

    public int maxMobsPerSpawner = 16;
    
    public boolean rangedSpawning = false;

    public int range=1000;
    
    public boolean needsPlayer = false;

    public int playerNeedRange=10000;
    
}

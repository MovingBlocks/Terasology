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
import org.terasology.entitySystem.EntityRef;

/**
 * Component that enables an entity to be spawned by something.
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class SpawnableComponent implements Component {

    /** What category is this spawnable */
    public String type = "undefined";
    
    /** Weight for how common the spawnable is, from 0-255 with 0 meaning unspawnable and 255 being the most common */
    public short probability = 1;

    /** What made this Spawnable? */
    public EntityRef parent = null;
    
    //TODO add darkness level and biome when map generation has reached better level
}

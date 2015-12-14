/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.propagation;

import org.terasology.math.geom.Vector3i;
import org.terasology.world.biomes.Biome;

/**
 * Used for notifying listeners that the biome at a spot in the world has changed.
 *
 */
public class BiomeChange {

    private final Vector3i position;
    private final Biome from;
    private Biome to;

    public BiomeChange(Vector3i position, Biome from, Biome to) {
        this.position = position;
        this.from = from;
        this.to = to;
    }

    public Vector3i getPosition() {
        return position;
    }

    public Biome getFrom() {
        return from;
    }

    public Biome getTo() {
        return to;
    }

    public void setTo(Biome to) {
        this.to = to;
    }

}

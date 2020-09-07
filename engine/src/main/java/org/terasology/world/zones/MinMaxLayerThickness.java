/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.zones;

import org.terasology.gestalt.module.sandbox.API;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.SimplexNoise;

/**
 * A {@link LayerThickness} that picks the thickness at each point by selecting a value between the minimum and maximum.
 *
 * This value is picked base on a noise function, so it translates smoothly between close points.
 */
@API
public class MinMaxLayerThickness extends SeededNoiseLayerThickness {

    private final int min;
    private final int max;

    /**
     * @param min the minimum thickness for the layer
     * @param max the maximum thickness for the layer
     */
    public MinMaxLayerThickness(int min, int max) {
        //TODO: make sure that layers at different heights have different noise
        super(seed -> new BrownianNoise(new SimplexNoise(seed), 2));

        this.min = min;
        this.max = max;
    }

    @Override
    public int get(int x, int z) {
        float noiseScale = 100f;
        float noiseValue = noise.noise(x / noiseScale, z / noiseScale);

        //Convert noise value to range [0..1]
        noiseValue = (noiseValue + 1) / 2;
        return Math.round(min + (noiseValue * (max - min)));
    }
}

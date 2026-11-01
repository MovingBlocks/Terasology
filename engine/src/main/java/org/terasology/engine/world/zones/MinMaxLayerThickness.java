// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.context.annotation.API;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;

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

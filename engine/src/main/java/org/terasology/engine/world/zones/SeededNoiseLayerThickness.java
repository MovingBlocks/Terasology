// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.context.annotation.API;
import org.terasology.engine.utilities.procedural.Noise;

import java.util.function.LongFunction;

/**
 * An abstract class for building a {@link LayerThickness} that is based on the value of a noise function.
 *
 * Note that the noise values are in the range [-1..1], so they must me adjusted to fit the desired layer thickness.
 */
@API
public abstract class SeededNoiseLayerThickness implements LayerThickness {

    protected Noise noise;
    private final LongFunction<Noise> seededNoiseFunction;

    public SeededNoiseLayerThickness(LongFunction<Noise> seededNoiseFunction) {
        this.seededNoiseFunction = seededNoiseFunction;
    }

    @Override
    public void initialize(LayeredZoneRegionFunction parent) {
        noise = seededNoiseFunction.apply(parent.getSeed());
    }

}

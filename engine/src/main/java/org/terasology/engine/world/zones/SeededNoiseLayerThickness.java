// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.gestalt.module.sandbox.API;

import java.util.function.LongFunction;

/**
 * An abstract class for building a {@link LayerThickness} that is based on the value of a noise function.
 * <p>
 * Note that the noise values are in the range [-1..1], so they must me adjusted to fit the desired layer thickness.
 */
@API
public abstract class SeededNoiseLayerThickness implements LayerThickness {

    private final LongFunction<Noise> seededNoiseFunction;
    protected Noise noise;

    public SeededNoiseLayerThickness(LongFunction<Noise> seededNoiseFunction) {
        this.seededNoiseFunction = seededNoiseFunction;
    }

    @Override
    public void initialize(LayeredZoneRegionFunction parent) {
        noise = seededNoiseFunction.apply(parent.getSeed());
    }

}

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
import org.terasology.utilities.procedural.Noise;

import java.util.function.LongFunction;

/**
 * An abstract class for building a {@link LayerThickness} that is based on the value of a noise function.
 *
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

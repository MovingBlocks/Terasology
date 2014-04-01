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
package org.terasology.world.generation.perlin;

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.providers.SurfaceHeightProvider;

/**
 * @author Immortius
 */
public class RiverProvider implements SurfaceHeightProvider {

    @Requires
    private SurfaceHeightProvider baseProvider;

    private BrownianNoise2D riverNoise;

    @Override
    public float getHeightAt(float x, float z) {
        float river = (float) TeraMath.clamp((java.lang.Math.sqrt(java.lang.Math.abs(riverNoise.noise(0.0008 * x, 0.0008 * z))) - 0.1) * 7.0);
        return baseProvider.getHeightAt(x, z) * TeraMath.clamp(river + 0.25f);
    }

    @Override
    public void setSeed(long seed) {
        riverNoise = new BrownianNoise2D(new SimplexNoise(seed + 2), 8);
    }
}

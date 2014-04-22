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
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.providers.SurfaceHeightProvider;

/**
 * @author Immortius
 */
public class PerlinRiverProvider implements SurfaceHeightProvider {

    @Requires
    private SurfaceHeightProvider baseProvider;

    private Noise3D riverNoise;

    @Override
    public float getHeightAt(float x, float z) {
        return baseProvider.getHeightAt(x, z) * (float) TeraMath.clamp((java.lang.Math.sqrt(Math.abs(riverNoise.noise(0.0008f * x, 0, 0.0008f * z))) - 0.1) * 7.0 + 0.25);
    }

    @Override
    public void setSeed(long seed) {
        riverNoise = new BrownianNoise3D(new PerlinNoise(seed + 2), 8);
    }
}

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

import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.generation.providers.SurfaceHeightProvider;

/**
 * @author Immortius
 */
public class BaseSurfaceProvider implements SurfaceHeightProvider {

    private BrownianNoise2D noise;

    @Override
    public float getHeightAt(float x, float z) {
        //return (float) noise.noise(x, z);
        return 32f + 32f * ((float) noise.noise(0.004 * x, 0.004 * z) + 1.0f) / 2.0f;
    }

    @Override
    public void setSeed(long seed) {
        noise = new BrownianNoise2D(new SimplexNoise(seed), 8);
    }
}

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
import org.terasology.world.generation.providers.SurfaceHeightProvider;

/**
 * @author Immortius
 */
public class PerlinSurfaceProvider implements SurfaceHeightProvider {

    private Noise3D surfaceNoise;

    @Override
    public float getHeightAt(float x, float z) {
        return 32f + 32f * (float) TeraMath.clamp((surfaceNoise.noise(0.004f * x, 0, 0.004f * z) + 1.0) / 2.0);
    }

    @Override
    public void setSeed(long seed) {
        surfaceNoise = new BrownianNoise3D(new PerlinNoise(seed), 8);
    }
}

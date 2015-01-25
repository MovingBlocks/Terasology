/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.procedural.FastNoise;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.random.FastRandom;

/**
 * A few tests for different {@link Noise3D} implementations.
 *
 * @author Martin Steiger
 */
public class NoiseTest {

    private static final Logger logger = LoggerFactory.getLogger(NoiseTest.class);

    @Test
    public void speedTest() {
        int seed = "asdf".hashCode();
        int count = 1000000;
        int warmUp = 10000;

        PerlinNoise pn = new PerlinNoise(seed);
        SimplexNoise sn = new SimplexNoise(seed);
        FastNoise fn = new FastNoise(seed);

        run(pn, warmUp);
        run(sn, warmUp);
        run(fn, warmUp);

        long start = System.nanoTime();

        run(pn, count);

        logger.info("Perlin Noise : " + (System.nanoTime() - start) / 1000000 + "ms.");

        start = System.nanoTime();

        run(sn, count);

        logger.info("Simplex Noise : " + (System.nanoTime() - start) / 1000000 + "ms.");

        start = System.nanoTime();

        run(fn, count);

        logger.info("Fast Noise : " + (System.nanoTime() - start) / 1000000 + "ms.");

    }

    private void run(Noise3D noise, int iterations) {
        FastRandom rng = new FastRandom(23479832);

        for (int i = 0; i < iterations; i++) {
            float posX = rng.nextFloat() * 1000f;
            float posY = rng.nextFloat() * 1000f;
            float posZ = rng.nextFloat() * 1000f;

            noise.noise(posX, posY, posZ);
        }
    }
}

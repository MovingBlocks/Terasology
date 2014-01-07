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
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.random.FastRandom;

/**
 * A simple test for {@link SimplexNoise}
 * @author Martin Steiger
 */
public class NoiseTest {

    private static final Logger logger = LoggerFactory.getLogger(NoiseTest.class);
    
    @Test
    public void speedTest() {
        int seed = "asdf".hashCode();
        int warmUp = 10000;
        int count = 1000000;
        FastRandom pfr = new FastRandom(seed);
        FastRandom sfr = new FastRandom(seed);
        
        PerlinNoise pn = new PerlinNoise(seed);
        SimplexNoise sn = new SimplexNoise(seed);
        
        for (int i = 0; i < warmUp; i++) {
            double posX = pfr.nextDouble() * 1000d;
            double posY = pfr.nextDouble() * 1000d;
            double posZ = pfr.nextDouble() * 1000d;

            pn.noise(posX, posY, posZ);
        }
        
        for (int i = 0; i < warmUp; i++) {
            double posX = sfr.nextDouble() * 1000d;
            double posY = sfr.nextDouble() * 1000d;
            double posZ = sfr.nextDouble() * 1000d;

            sn.noise(posX, posY, posZ);
        }

        long start = System.nanoTime();
        
        for (int i = 0; i < count; i++) {
            double posX = pfr.nextDouble() * 1000d;
            double posY = pfr.nextDouble() * 1000d;
            double posZ = pfr.nextDouble() * 1000d;

            pn.noise(posX, posY, posZ);
        }
        
        logger.info("Perlin Noise : " + (System.nanoTime() - start) / 1000000 + "ms.");

        start = System.nanoTime();
        
        for (int i = 0; i < count; i++) {
            double posX = sfr.nextDouble() * 1000d;
            double posY = sfr.nextDouble() * 1000d;
            double posZ = sfr.nextDouble() * 1000d;

            sn.noise(posX, posY, posZ);
        }
        
        logger.info("Simplex Noise : " + (System.nanoTime() - start) / 1000000 + "ms.");
        
    }
}

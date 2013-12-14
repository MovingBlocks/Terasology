/*
 * Copyright 2013 MovingBlocks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.DiscreteWhiteNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * A few tests for different {@link Noise} implementations.
 *
 */
@RunWith(Parameterized.class)
public class NoiseTest {

    private Noise noiseGen;
    private Random rng;

    public NoiseTest(Noise noiseGen) {
        this.noiseGen = noiseGen;
        this.rng = new FastRandom(0xBEEF);
    }

    @Parameters(name = "{0}")
    public static Collection<Noise[]> data() {
        return Arrays.asList(new Noise[][]{
                { new WhiteNoise(0xCAFE) },
                { new DiscreteWhiteNoise(0xCAFE) },
                { new SimplexNoise(0xCAFE) },
                { new PerlinNoise(0xCAFE) },
                { new BrownianNoise(new WhiteNoise(0xCAFE), 3) }
        });
    }

    @Test
    public void testMinMax() {

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < 5000000; i++) {
            float posX = rng.nextFloat() * 100f;
            float posY = rng.nextFloat() * 100f;
            float posZ = rng.nextFloat() * 100f;
            float noise = noiseGen.noise(posX, posY, posZ);

            if (noise < min) {
                min = noise;
            }

            if (noise > max) {
                max = noise;
            }
        }

        Assert.assertTrue(min >= -1);
        Assert.assertTrue(max <=  1);

        Assert.assertEquals(-1, min, 0.05);
        Assert.assertEquals(1, max, 0.05);
    }

    @Test
    public void resolutionTest() {

        for (int i = 0; i < 1000000; i++) {
            float posX = rng.nextFloat() * 100f;
            float posY = rng.nextFloat() * 100f;
            float posZ = rng.nextFloat() * 100f;

            float noise = noiseGen.noise(posX, posY, posZ);
            if (noise > 0 && noise < 0.00005) {
                return;
            }
        }

        Assert.fail();
    }
}

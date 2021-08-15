// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.utilities;

import org.junit.jupiter.api.Test;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.utilities.random.FastRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple validity test for {@link WhiteNoise}
 */
public class WhiteNoiseTest {

    @Test
    public void testDistribution() {
        FastRandom rng = new FastRandom(0xBEEF);
        WhiteNoise noiseGen = new WhiteNoise(0xDEADC0DE);

        int count = 100000;
        int bucketCount = 20;
        int[] buckets = new int[bucketCount];

        for (int i = 0; i < count; i++) {
            float posX = rng.nextFloat() * 100f;
            float posY = rng.nextFloat() * 100f;
            float posZ = rng.nextFloat() * 100f;

            float noise = 0.5f + 0.5f * noiseGen.noise(posX, posY, posZ);
            int idx = (int) (noise * bucketCount);
            if (idx == bucketCount) {
                idx = bucketCount - 1;
            }
            buckets[idx]++;
        }

        float avg = count / bucketCount;

        for (int i = 0; i < bucketCount; i++) {
            float val = Math.abs((buckets[i] - avg) / avg);
            // less than 5% deviation from the expected average
            assertTrue(val < 0.05);
        }
    }
}


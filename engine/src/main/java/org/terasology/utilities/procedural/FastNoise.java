/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.utilities.procedural;

import org.terasology.math.TeraMath;

/**
 * Produces fast, <b>equal-distributed white noise</b> based on two {@link NoiseTable}s.
 * Using two noise tables gives a noise resolution of 1/65535, which should be sufficient
 * for most applications.
 * Noise is created at discrete intervals only, i.e. noise(3.4) == noise(2.6) == noise(2).
 * @author Martin Steiger
 * @deprecated Use {@link WhiteNoise} or {@link DiscreteWhiteNoise} instead. They are superior in every aspect.
 */
@Deprecated
public class FastNoise implements Noise2D, Noise3D {

    private final NoiseTable table1;
    private final NoiseTable table2;

    public FastNoise(long seed) {
        table1 = new NoiseTable(seed + 0);
        table2 = new NoiseTable(seed + 1);
    }

    /**
     * Coordinates will be rounded to the closest integer value.
     * @return equal-distributed white noise in the range [0..1] at a resolution of 0.000015
     */
    @Override
    public float noise(float x, float y, float z) {
        int ix = TeraMath.floorToInt(x + 0.5);
        int iy = TeraMath.floorToInt(y + 0.5);
        int iz = TeraMath.floorToInt(z + 0.5);

        int in1 = table1.noise(ix, iy, iz);
        int in2 = table2.noise(ix, iy, iz);
        int in = (in1 << 8) | in2;

        return in / 65535.0f;
    }

    /**
     * Coordinates will be rounded to the closest integer value.
     * @return equal-distributed white noise in the range [0..1] at a resolution of 0.000015
     */
    @Override
    public float noise(float x, float y) {
        int ix = TeraMath.floorToInt(x + 0.5);
        int iy = TeraMath.floorToInt(y + 0.5);

        int in1 = table1.noise(ix, iy);
        int in2 = table2.noise(ix, iy);
        int in = (in1 << 8) | in2;

        return in / 65535.0f;
    }

}

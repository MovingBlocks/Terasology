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

package org.terasology.utilities.procedural;

/**
 * Computes Brownian noise based on some noise generator.
 * Originally, Brown integrates white noise, but using other noises can be sometimes useful, too.
 * @deprecated Use {@link BrownianNoise} instead and adjust the scale factor: the new impl. returns [-1..1].
 */
@Deprecated
public class BrownianNoise3D extends BrownianNoiseOld implements Noise3D {

    private final Noise3D other;

    /**
     * Uses the default number of octaves
     * @param other the noise to use as a basis
     */
    public BrownianNoise3D(Noise3D other) {
        this.other = other;
    }

    /**
     * @param octaves the number of octaves to use
     */
    public BrownianNoise3D(Noise3D other, int octaves) {
        this(other);
        setOctaves(octaves);
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise value in the range [-getScale()..getScale()]
     */
    @Override
    public float noise(float x, float y, float z) {
        float result = 0.0f;

        float workingX = x;
        float workingY = y;
        float workingZ = z;
        for (int i = 0; i < getOctaves(); i++) {
            result += other.noise(workingX, workingY, workingZ) * getSpectralWeight(i);

            workingX *= getLacunarity();
            workingY *= getLacunarity();
            workingZ *= getLacunarity();
        }

        return result;
    }

}

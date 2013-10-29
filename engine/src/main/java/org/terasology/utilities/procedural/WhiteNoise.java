/*
 * Copyright 2013 MovingBlocks
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
 * Some white noise
 *
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public class WhiteNoise implements Noise {

    private static final double LACUNARITY = 2.1379201;
    private static final double H = 0.836281;

    private double[] spectralWeights;

    private boolean recomputeSpectralWeights = true;
    private int octaves = 9;
    private Random rand;
    private double amplitude;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public WhiteNoise(int seed, double amplitude1) {
        rand = new FastRandom(seed);

        if (amplitude > 1) {
            amplitude = 1 / amplitude1;
        }
        if (amplitude < 0) {
            amplitude = -amplitude1;
        }
    }

    /**
     * Returns the noise value at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise value
     */
    public double noise(double x, double y, double z) {
        return (rand.nextDouble(-1.0f, 1.0f) % 256) * amplitude;
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise value
     */
    public double fBm(double x, double y, double z) {
        double result = 0.0;

        if (recomputeSpectralWeights) {
            spectralWeights = new double[octaves];

            for (int i = 0; i < octaves; i++) {
                spectralWeights[i] = java.lang.Math.pow(LACUNARITY, -H * i);
            }

            recomputeSpectralWeights = false;
        }

        double workingX = x;
        double workingY = y;
        double workingZ = z;
        for (int i = 0; i < octaves; i++) {
            result += noise(workingX, workingY, workingZ) * spectralWeights[i];

            workingX *= LACUNARITY;
            workingY *= LACUNARITY;
            workingZ *= LACUNARITY;
        }

        return result;
    }

    public void setOctaves(int octaves) {
        this.octaves = octaves;
        recomputeSpectralWeights = true;
    }

    public int getOctaves() {
        return octaves;
    }
}

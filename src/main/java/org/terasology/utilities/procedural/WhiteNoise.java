/*
 * Copyright 2013 Moving Blocks
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

    private double[] _spectralWeights;

    private boolean _recomputeSpectralWeights = true;
    private int _octaves = 9;
    private FastRandom rand;
    private double amplitude;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public WhiteNoise(int seed, double amplitude1) {
        rand = new FastRandom(seed);

        if (amplitude > 1)
            amplitude = 1 / amplitude1;
        if (amplitude < 0)
            amplitude = -amplitude1;
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
        return (rand.randomDouble() % 256) * amplitude;
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

        if (_recomputeSpectralWeights) {
            _spectralWeights = new double[_octaves];

            for (int i = 0; i < _octaves; i++)
                _spectralWeights[i] = java.lang.Math.pow(LACUNARITY, -H * i);

            _recomputeSpectralWeights = false;
        }

        for (int i = 0; i < _octaves; i++) {
            result += noise(x, y, z) * _spectralWeights[i];

            x *= LACUNARITY;
            y *= LACUNARITY;
            z *= LACUNARITY;
        }

        return result;
    }

    public void setOctaves(int octaves) {
        _octaves = octaves;
        _recomputeSpectralWeights = true;
    }

    public int getOctaves() {
        return _octaves;
    }
}

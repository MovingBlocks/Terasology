// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.procedural;

/**
 * Computes Brownian noise based on some noise generator.
 * Originally, Brown integrates white noise, but using other noises can be sometimes useful, too.
 */
public abstract class BrownianNoiseOld {

    /**
     * Default persistence value
     */
    public static final double DEFAULT_PERSISTENCE = 0.836281;

    /**
     * Default lacunarity value
     */
    public static final double DEFAULT_LACUNARITY = 2.1379201;

    private double lacunarity = DEFAULT_LACUNARITY;

    private double persistence = DEFAULT_PERSISTENCE;

    private int octaves;
    private double[] spectralWeights;

    /**
     * Initialize with 9 octaves - <b>this is insanely expensive, but backwards compatible</b>
     */
    protected BrownianNoiseOld() {
        setOctaves(9);
    }

    /**
     * Values of noise() are in the range [-scale..scale]
     * @return the scale
     */
    public double getScale() {
        double sum = 0;
        for (double weight : spectralWeights) {
            sum += weight;
        }
        return sum;
    }

    /**
     * @param octaves the number of octaves used for computation
     */
    public void setOctaves(int octaves) {
        this.octaves = octaves;

        // recompute weights eagerly
        spectralWeights = new double[octaves];

        for (int i = 0; i < octaves; i++) {
            spectralWeights[i] = Math.pow(lacunarity, -persistence * i);
        }
   }

    /**
     * @return the number of octaves
     */
    public int getOctaves() {
        return octaves;
    }

    /**
     * Lacunarity is what makes the frequency grow. Each octave
     * the frequency is multiplied by the lacunarity.
     * @return the lacunarity
     */
    public double getLacunarity() {
        return this.lacunarity;
    }

    /**
     * Lacunarity is what makes the frequency grow. Each octave
     * the frequency is multiplied by the lacunarity.
     * @param lacunarity the lacunarity
     */
    public void setLacunarity(double lacunarity) {
        this.lacunarity = lacunarity;
    }

    /**
     * Persistence is what makes the amplitude shrink.
     * More precicely the amplitude of octave i = lacunarity^(-persistence * i)
     * @return the persistance
     */
    public double getPersistance() {
        return this.persistence;
    }

    /**
     * Persistence is what makes the amplitude shrink.
     * More precisely the amplitude of octave i = lacunarity^(-persistence * i)
     * @param persistence the persistence to set
     */
    public void setPersistence(double persistence) {
        this.persistence = persistence;
    }

    /**
     * @return the spectralWeights
     */
    protected double getSpectralWeight(int octave) {
        return spectralWeights[octave];
    }

}

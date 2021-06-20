// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.procedural;

/**
 * Computes Brownian noise based on some noise generator.
 * Originally, Brown integrates white noise, but using other noises can be sometimes useful, too.
 */
public class BrownianNoise extends AbstractNoise {

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
    private float[] spectralWeights;
    private float scale;                // 1/sum of all weights
    private final Noise other;

    /**
     * Initialize with 9 octaves - <b>this is quite expensive, but backwards compatible</b>
     * @param other the noise to use as a basis
     */
    public BrownianNoise(Noise other) {
        this(other, 9);
    }

    /**
     * @param other other the noise to use as a basis
     * @param octaves the number of octaves to use
     */
    public BrownianNoise(Noise other, int octaves) {
        this.other = other;
        setOctaves(octaves);
    }

    /**
     * 1/sum of all weights
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @return The noise value in the range of the base noise function
     */
    @Override
    public float noise(float x, float y) {
        float result = 0.0f;

        float workingX = x;
        float workingY = y;
        for (int i = 0; i < getOctaves(); i++) {
            result += other.noise(workingX, workingY) * spectralWeights[i];

            workingX *= (float) getLacunarity();
            workingY *= (float) getLacunarity();

            // Include random offsets so that the origins of all the octaves don't all add up and make a weird feature there.
            workingX += 10 * other.noise(i + 0.5f, 0.5f);
            workingY += 10 * other.noise(-i - 0.5f, -0.5f);
        }

        return result * scale;
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise value in the range of the base noise function
     */
    @Override
    public float noise(float x, float y, float z) {
        float result = 0.0f;

        float workingX = x;
        float workingY = y;
        float workingZ = z;
        for (int i = 0; i < getOctaves(); i++) {
            result += other.noise(workingX, workingY, workingZ) * spectralWeights[i];

            workingX *= (float) getLacunarity();
            workingY *= (float) getLacunarity();
            workingZ *= (float) getLacunarity();
        }

        return result * scale;
    }

    private static float computeScale(float[] spectralWeights) {
        float sum = 0;
        for (float weight : spectralWeights) {
            sum += weight;
        }
        return 1.0f / sum;
    }

    /**
     * @param octaves the number of octaves used for computation
     */
    public void setOctaves(int octaves) {
        this.octaves = octaves;
        updateWeights();
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
        updateWeights();
    }

    private void updateWeights() {
        // recompute weights eagerly
        spectralWeights = new float[octaves];

        for (int i = 0; i < octaves; i++) {
            spectralWeights[i] = (float) Math.pow(lacunarity, -persistence * i);
        }

        scale = computeScale(spectralWeights);
   }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.random;

import org.terasology.context.annotation.API;

import ec.util.MersenneTwisterFast;

/**
 * Random number generator based on the Mersenne Primer Twister implementation of Sean Luke.
 * The MersenneTwister code is based on standard MT19937 C/C++ code by Takuji Nishimura.<br><br>
 * Reference: Makato Matsumoto and Takuji Nishimura: <br>
 * "Mersenne Twister: A 623-Dimensionally Equidistributed Uniform Pseudo-Random Number Generator"
 *
 */
@API
public class MersenneRandom extends Random {

    private final MersenneTwisterFast mersenne;

    /**
     * Initializes a new instance of the random number generator using a
     * specified seed.
     * 
     * @param seed The seed to use
     */
    public MersenneRandom(long seed) {
        mersenne = new MersenneTwisterFast(seed);
    }

    /**
     * Initializes a new instance of the random number generator using
     * "System.currentTimeMillis()" as seed.
     */
    public MersenneRandom() {
        mersenne = new MersenneTwisterFast();
    }

    /**
     * Constructor using an array of integers as seed. Your array must have a
     * non-zero length. Only the first 624 integers in the array are used; if
     * the array is shorter than this then integers are repeatedly used in a
     * wrap-around fashion.
     * @param array the seed value
     */
    public MersenneRandom(int[] array) {
        mersenne = new MersenneTwisterFast(array);
    }

    @Override
    public int nextInt() {
        return mersenne.nextInt();
    }

    @Override
    public int nextInt(int max) {
        return mersenne.nextInt(max);
    }

    @Override
    public long nextLong() {
        return mersenne.nextLong();
    }

    @Override
    public long nextLong(long max) {
        return mersenne.nextLong(max);
    }

    @Override
    public float nextFloat() {
        return mersenne.nextFloat();
    }

    @Override
    public double nextDouble() {
        return mersenne.nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return mersenne.nextBoolean();
    }

    @Override
    public double nextGaussian() {
        return mersenne.nextGaussian();
    }

}

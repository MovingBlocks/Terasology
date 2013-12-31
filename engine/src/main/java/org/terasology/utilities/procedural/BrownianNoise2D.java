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

package org.terasology.utilities.procedural;

/**
 * Computes Brownian noise based on some noise generator.
 * Originally, Brown integrates white noise, but using other noises can be sometimes useful, too.
 * @author Martin Steiger
 */
public class BrownianNoise2D extends BrownianNoise implements Noise2D {

    private final Noise2D other;
    
    /**
     * Uses the default number of octaves
     * @param other the noise to use as a basis
     */
    public BrownianNoise2D(Noise2D other) {
        this.other = other; 
    }

    /**
     * @param octaves the number of octaves to use
     */
    public BrownianNoise2D(Noise2D other, int octaves) {
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
    public double noise(double x, double y) {
        double result = 0.0;

        double workingX = x;
        double workingY = y;
        for (int i = 0; i < getOctaves(); i++) {
            result += other.noise(workingX, workingY) * getSpectralWeight(i);

            workingX *= getLacunarity();
            workingY *= getLacunarity();
        }

        return result;
    }
    
}

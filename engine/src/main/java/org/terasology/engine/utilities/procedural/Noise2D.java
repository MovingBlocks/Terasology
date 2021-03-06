// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.procedural;

/**
 * Provides or generates 2D noise
 *
 * @deprecated use {@link Noise} instead
 */
@Deprecated
public interface Noise2D {

    /**
     * Returns the noise value at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @return The noise value
     */
    float noise(float x, float y);
}

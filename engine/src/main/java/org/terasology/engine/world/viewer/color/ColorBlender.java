// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.color;


/**
 * Maps or blends color from a source into a target color space.
 */
public interface ColorBlender {

    /**
     * Converts a color into target space.
     * @param src the source color in an implementation-dependent color space
     * @return the color in the target color space
     */
    int get(int src);

    /**
     * Adds a color from the source space on top of a color in dst color space
     * @param src the source color in an implementation-dependent color space
     * @param dst the dest. color
     * @return the color (src + dst) in the target color space
     */
    int add(int src, int dst);

    /**
     * Blends a color using source alpha from the source space on top of a color in dst color space
     * @param src the source color in an implementation-dependent color space
     * @param dst the dest. color
     * @return the color (a * s + (1 - a) * dst) in the target color space
     */
    int blend(int src, int dst);
}

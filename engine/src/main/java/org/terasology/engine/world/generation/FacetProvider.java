// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

/**
 */
public interface FacetProvider {

    /**
     * @param seed the seed value (typically used for random number generators)
     */
    default void setSeed(long seed) {
        // don't do anything
    }

    /**
     * This is always called after {@link #setSeed(long)}.
     */
    default void initialize() {
        // don't do anything
    }

    void process(GeneratingRegion region);
}

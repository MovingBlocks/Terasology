// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers;

/**
 * Rendering hint that is used in Painter's algorithm.
 * A Higher value indicates that the image should be drawn on top.
 */
public final class ZOrder {

    public static final int BIOME      =     0;
    public static final int SURFACE    =  1000;
    public static final int FLORA      =  2000;
    public static final int TREES      =  3000;

    private ZOrder() {
        // no instances
    }
}

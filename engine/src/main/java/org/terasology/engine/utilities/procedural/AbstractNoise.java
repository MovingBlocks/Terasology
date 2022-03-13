// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.procedural;

/**
 * An abstract implementation of most methods.
 * The int-based methods delegate to float-bases ones.
 */
public abstract class AbstractNoise implements Noise {

    @Override
    public float noise(int x, int y) {
        return noise((float) x, (float) y);
    }

    @Override
    public float noise(int x, int y, int z) {
        return noise((float) x, (float) y, (float) z);
    }

    @Override
    public float noise(float x, float y) {
        return noise(x, y, 0);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import org.terasology.nui.Colorc;

/**
 * A system for looking up foliage and grass colors at a block, used by DefaultColorSource.
 */
public interface ColorProvider {
    /**
     * Looks up the color for grass at a given position.
     */
    Colorc colorLut(int x, int y, int z);

    /**
     * Looks up the color for foliage at a given position.
     */
    Colorc foliageLut(int x, int y, int z);
}

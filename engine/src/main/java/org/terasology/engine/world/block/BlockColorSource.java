// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import org.terasology.engine.world.ChunkView;
import org.terasology.nui.Colorc;

/**
 * Used to determine a multiplicative color for certain blocks based on the block's world conditions.
 */
@FunctionalInterface
public interface BlockColorSource {

    default Colorc calcColor(ChunkView view) {
        return calcColor(view, 0, 0, 0);
    };

    default Colorc calcColor(ChunkView view, int x, int z) {
        return calcColor(view, x, 0, z);
    };

    /**
     * @return the color that should be applied at the coordinates {@code (x, y, z)} relative to {@code view}.
     */
    Colorc calcColor(ChunkView view, int x, int y, int z);

}

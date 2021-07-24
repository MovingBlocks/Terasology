// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import org.terasology.nui.Colorc;

/**
 * Used to determine a multiplicative color for certain blocks based on the block's world conditions.
 */
@FunctionalInterface
public interface BlockColorSource {

    default Colorc calcColor() {
        return calcColor(0, 0, 0);
    };

    default Colorc calcColor(int x, int z) {
        return calcColor(x, 0, z);
    };

    Colorc calcColor(int x, int y, int z);

}

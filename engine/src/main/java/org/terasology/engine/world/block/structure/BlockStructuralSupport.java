// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.structure;

import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

import java.util.Map;

public interface BlockStructuralSupport {
    /**
     * Returns priority of this check. All checks with higher priority (lower number) will be invoked before checks with
     * lower priority (higher number).
     *
     * @return
     */
    int getPriority();

    /**
     * Checks if the block at specified location is sufficiently supported according to this class.
     *
     * @param location Location of the block to check.
     * @param sideChanged Side that has triggered this check.
     * @return If the block should be immediately removed.
     */
    boolean shouldBeRemovedDueToChange(Vector3i location, Side sideChanged);

    /**
     * Checks if the block at location is sufficiently supported.
     *
     * @param location
     * @param blockOverrides
     * @return
     */
    boolean isSufficientlySupported(Vector3i location, Map<Vector3i, Block> blockOverrides);
}

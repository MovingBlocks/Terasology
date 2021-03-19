// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;

/**
 * A view providing access to the world specifically for batch propagation
 */
public interface PropagatorWorldView {

    byte UNAVAILABLE = -1;

    /**
     * @return The value of interest at pos, or {@link #UNAVAILABLE} if out of bounds
     */
    byte getValueAt(Vector3ic pos);

    /**
     * @param value A new value at pos.
     */
    void setValueAt(Vector3ic pos, byte value);

    /**
     * @return The block at pos, or null if out of bounds
     */
    Block getBlockAt(Vector3ic pos);

}

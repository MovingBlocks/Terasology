// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * Used for entities representing a block in the world
 */
public final class BlockComponent implements Component {
    @Replicate
    Vector3i position = new Vector3i();
    @Replicate
    public Block block;

    public BlockComponent() {
    }

    public BlockComponent(Block block, Vector3ic pos) {
        this.block = block;
        this.position.set(pos);
    }

    /**
     * Get an immutable view on the current position.
     *
     * Note: the vector may change when the position on this component is updated. If the position information is to be
     * stored you should use {@link #getPosition(Vector3i)} instead.
     */
    public Vector3ic getPosition() {
        return position;
    }

    /**
     * get the position
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getPosition(Vector3i dest) {
        dest.set(position);
        return dest;
    }

    public void setPosition(Vector3ic pos) {
        position.set(pos);
    }
}

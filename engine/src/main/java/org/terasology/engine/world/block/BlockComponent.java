// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.math.geom.Vector3i;

/**
 * Used for entities representing a block in the world
 */
public final class BlockComponent implements Component {
    @Replicate
    public Vector3i position = new Vector3i();
    @Replicate
    public Block block;

    public BlockComponent() {
    }

    public BlockComponent(Block block, Vector3i pos) {
        this.block = block;
        this.position.set(pos);
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is error prone (no defensive copy) and needlessly verbose.
     */
    @Deprecated
    public Vector3i getPosition() {
        return position;
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is needlessly verbose.
     */
    @Deprecated
    public void setPosition(Vector3i pos) {
        position.set(pos);
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is error prone (no defensive copy) and needlessly verbose.
     */
    @Deprecated
    public Block getBlock() {
        return block;
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is error prone (no defensive copy) and needlessly verbose.
     */
    @Deprecated
    public void setBlock(Block block) {
        this.block = block;
    }
}

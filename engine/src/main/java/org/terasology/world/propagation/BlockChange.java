// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;

/**
 * Represents a block change at a given position. Used to update listeners that a block has been changed
 * <p>
 * A POJO hence no methods are documented.
 */
public class BlockChange {
    private final Vector3i position = new Vector3i();
    private final Block from;
    private Block to;

    public BlockChange(Vector3ic position, Block from, Block to) {
        this.position.set(position);
        this.from = from;
        this.to = to;
    }

    public Vector3ic getPosition() {
        return position;
    }

    public Block getFrom() {
        return from;
    }

    public Block getTo() {
        return to;
    }

    public void setTo(Block block) {
        this.to = block;
    }
}

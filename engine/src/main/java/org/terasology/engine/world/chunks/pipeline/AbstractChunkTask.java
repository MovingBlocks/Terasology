// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.terasology.math.geom.Vector3i;

/**
 *
 */
public abstract class AbstractChunkTask implements ChunkTask {
    private final Vector3i position;

    public AbstractChunkTask(Vector3i position) {
        this.position = new Vector3i(position);
    }

    @Override
    public Vector3i getPosition() {
        return position;
    }

    @Override
    public boolean isTerminateSignal() {
        return false;
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.terasology.math.geom.Vector3i;

/**
 *
 */
public final class ShutdownChunkTask implements ChunkTask {

    @Override
    public String getName() {
        return "Shutdown";
    }

    @Override
    public void run() {
    }

    @Override
    public boolean isTerminateSignal() {
        return true;
    }

    @Override
    public Vector3i getPosition() {
        return Vector3i.zero();
    }

}

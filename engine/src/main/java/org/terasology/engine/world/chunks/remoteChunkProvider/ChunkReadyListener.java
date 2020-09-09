// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.remoteChunkProvider;

import org.terasology.math.geom.Vector3i;

@FunctionalInterface
public interface ChunkReadyListener {
    void onChunkReady(Vector3i pos);
}

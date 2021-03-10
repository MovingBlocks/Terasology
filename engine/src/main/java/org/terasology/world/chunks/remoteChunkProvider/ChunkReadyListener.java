// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.remoteChunkProvider;

import org.joml.Vector3ic;

@FunctionalInterface
public interface ChunkReadyListener {
    void onChunkReady(Vector3ic pos);
}

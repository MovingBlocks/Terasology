// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

public interface ChunkTaskListener {

    void onDone(ChunkTask chunkTask);
}

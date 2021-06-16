// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;

import java.util.Set;

public interface InitialChunkProvider {
    boolean hasNext();

    Chunk next(Set<Vector3ic> currentlyGenerating);
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.terasology.engine.math.AABB;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.gestalt.module.sandbox.API;

/**
 *
 */
@API
public interface RenderableChunk extends LitChunk {

    boolean isDirty();

    void setDirty(boolean dirty);

    AABB getAABB();

    boolean isAnimated();

    void setAnimated(boolean animated);

    boolean hasMesh();

    boolean hasPendingMesh();

    ChunkMesh getMesh();

    void setMesh(ChunkMesh newMesh);

    ChunkMesh getPendingMesh();

    void setPendingMesh(ChunkMesh newPendingMesh);

    void disposeMesh();

    boolean isReady();

}

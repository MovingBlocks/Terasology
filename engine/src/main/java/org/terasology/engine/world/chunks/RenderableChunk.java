// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.chunks;

import org.terasology.joml.geom.AABBfc;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.primitives.ChunkMesh;

/**
 */
@API
public interface RenderableChunk extends LitChunk {

    boolean isDirty();

    void setDirty(boolean dirty);

    AABBfc getAABB();

    void setMesh(ChunkMesh newMesh);

    void setPendingMesh(ChunkMesh newPendingMesh);

    void setAnimated(boolean animated);

    boolean isAnimated();

    boolean hasMesh();

    boolean hasPendingMesh();

    ChunkMesh getMesh();

    ChunkMesh getPendingMesh();

    void disposeMesh();

    boolean isReady();

}

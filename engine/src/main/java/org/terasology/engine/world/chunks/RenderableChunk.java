// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.joml.Vector3f;
import org.terasology.joml.geom.AABBfc;
import org.terasology.context.annotation.API;
import org.terasology.engine.rendering.primitives.ChunkMesh;

/**
 * Anything that acts like a chunk for rendering purposes
 */
@API
public interface RenderableChunk {

    Vector3f getRenderPosition();

    AABBfc getAABB();

    void setMesh(ChunkMesh newMesh);

    void setAnimated(boolean animated);

    boolean isAnimated();

    boolean hasMesh();

    ChunkMesh getMesh();

    void disposeMesh();

    boolean isReady();

}

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

    /**
     * Squared distance from this chunk's render position to the given point, without allocating
     * a temporary vector. Implementations that can compute their render position from primitive
     * fields should override this; the default falls back to {@link #getRenderPosition()}.
     */
    default float distanceSquared(float x, float y, float z) {
        Vector3f renderPosition = getRenderPosition();
        float dx = renderPosition.x - x;
        float dy = renderPosition.y - y;
        float dz = renderPosition.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    AABBfc getAABB();

    void setMesh(ChunkMesh newMesh);

    void setAnimated(boolean animated);

    boolean isAnimated();

    boolean hasMesh();

    ChunkMesh getMesh();

    void disposeMesh();

    boolean isReady();

}

// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.terasology.engine.network.Replicate;
import org.terasology.engine.rendering.primitives.MutableChunkMesh;
import org.terasology.joml.geom.AABBf;

/**
 * An entity that should be rendered as though it was a chunk.
 * Requires a LocationComponent as well in order to actually be rendered.
 */
public class ChunkMeshComponent implements VisualComponent<ChunkMeshComponent> {
    @Replicate
    public MutableChunkMesh mesh;
    @Replicate
    public AABBf aabb;
    @Replicate
    public boolean animated = true;

    public ChunkMeshComponent() { }

    public ChunkMeshComponent(MutableChunkMesh mesh, AABBf aabb) {
        this.mesh = mesh;
        this.aabb = aabb;
    }

    public synchronized void setMesh(MutableChunkMesh mesh) {
        if (this.mesh != null) {
            this.mesh.dispose();
        }
        this.mesh = mesh;
    }

    @Override
    public void copyFrom(ChunkMeshComponent other) {
        this.mesh = other.mesh; // TODO deep or shallow copy?
        this.aabb = new AABBf(other.aabb);
        this.animated = other.animated;
    }
}

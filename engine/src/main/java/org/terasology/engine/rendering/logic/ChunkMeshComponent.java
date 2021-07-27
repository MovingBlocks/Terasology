// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.terasology.engine.network.Replicate;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.joml.geom.AABBf;

/**
 * An entity that should be rendered as though it was a chunk.
 * Requires a LocationComponent as well in order to actually be rendered.
 */
public class ChunkMeshComponent implements VisualComponent {
    @Replicate
    public ChunkMesh mesh;
    @Replicate
    public AABBf aabb;
    @Replicate
    public boolean animated = true;

    public ChunkMeshComponent() { }

    public ChunkMeshComponent(ChunkMesh mesh, AABBf aabb) {
        this.mesh = mesh;
        this.aabb = aabb;
    }
}

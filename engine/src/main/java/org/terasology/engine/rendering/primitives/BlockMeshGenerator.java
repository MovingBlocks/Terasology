// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import org.terasology.context.annotation.API;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.world.ChunkView;

/**
 * This is used to generate Mesh data from a block in a chunk to a ChunkMesh output.
 */
@API
public interface BlockMeshGenerator {

    /**
     * Generates a block mesh at the defined location in the ChunkMesh.
     *
     * @param view  The input hunk area to acquire the Block data from.
     * @param mesh  The output mesh that is being generated.
     * @param x     Input position X.
     * @param y     Input position Y.
     * @param z     Input position Z.
     */
    void generateChunkMesh(ChunkView view, ChunkMesh mesh, int x, int y, int z);

    /**
     * @return A standalone mesh used for items, inventory, etc...
     */
    Mesh getStandaloneMesh();
}

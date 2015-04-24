/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.primitives;

import org.terasology.module.sandbox.API;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.world.ChunkView;

/**
 * This is used to generate Mesh data from a block in a chunk to a ChunkMesh output.
 *
 * Created by overminddl1 on 4/15/15.
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

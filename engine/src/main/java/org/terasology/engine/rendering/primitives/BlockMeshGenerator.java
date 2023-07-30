// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.engine.rust.resource.ChunkGeometry;
import org.terasology.gestalt.module.sandbox.API;
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
     * @param view The input hunk area to acquire the Block data from.
     * @param mesh The output mesh that is being generated.
     * @param x Input position X.
     * @param y Input position Y.
     * @param z Input position Z.
     */
    void generateChunkMesh(ChunkView view, ChunkMeshBufferGroup mesh, int x, int y, int z);

    /**
     * @return A standalone mesh used for items, inventory, etc...
     */
    Mesh getStandaloneMesh();


    final class ChunkMeshBuffer {
        public final VertexResource positionBuffer;
        public int cursor = 0;
        public final VertexAttributeBinding<Vector3fc, Vector3f> position;
        public final IndexResource indices = new IndexResource();
//        public final VertexResource NormalBuffer;
//        public final VertexResource uvBuffer;
//        public final VertexResource ColroBuffer;
//        public final VertexResource AttributeBuffer;

        public ChunkMeshBuffer() {
//                {
            VertexResourceBuilder builder = new VertexResourceBuilder();
            position = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
            positionBuffer = builder.build();
//                }
        }
    }
    final class ChunkMeshBufferGroup {
        public final ChunkMeshBuffer opaque = new ChunkMeshBuffer();
        public final ChunkMeshBuffer translucent = new ChunkMeshBuffer();
        public final ChunkMeshBuffer billboard = new ChunkMeshBuffer();
        public final ChunkMeshBuffer waterAndIce = new ChunkMeshBuffer();
    }

}

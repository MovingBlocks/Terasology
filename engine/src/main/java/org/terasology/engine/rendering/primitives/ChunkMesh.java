// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexFloatAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.context.annotation.API;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

/**
 * Chunk meshes store, manipulate and render the vertex data of tessellated chunks.
 */
public interface ChunkMesh {

    VertexElements getVertexElements(ChunkMesh.RenderType renderType);

    /**
     * has any vertex elements for is cleared from {@link #discardData()}
     *
     * @return if the vertex data is cleared
     */
    boolean hasVertexElements();

    /**
     * update the mesh data
     *
     * @return true if the data has been updated
     */
    boolean updateMesh();

    void discardData();

    void updateMaterial(Material chunkMaterial, Vector3fc chunkPosition, boolean chunkIsAnimated);

    int triangleCount(ChunkMesh.RenderPhase phase);

    int getTimeToGenerateBlockVertices();

    int getTimeToGenerateOptimizedBuffers();

    void dispose();

    int render(ChunkMesh.RenderPhase type);

    /**
     * Possible rendering types.
     */
    @API
    enum RenderType {
        OPAQUE(0),
        TRANSLUCENT(1),
        BILLBOARD(2),
        WATER_AND_ICE(3);

        private final int meshIndex;

        RenderType(int index) {
            meshIndex = index;
        }

        public int getIndex() {
            return meshIndex;
        }
    }

    enum RenderPhase {
        OPAQUE,
        ALPHA_REJECT,
        REFRACTIVE,
        Z_PRE_PASS
    }

    class VertexElements {

        public static final int VERTEX_INDEX = 0; // vec3
        public static final int NORMAL_INDEX = 1;  // vec3
        public static final int UV0_INDEX = 2;  // vec3

        public static final int FLAGS_INDEX = 3;  // int
        public static final int FRAME_INDEX = 4; // float

        public static final int SUNLIGHT_INDEX = 5; // float
        public static final int BLOCK_INDEX = 6; // float
        public static final int AMBIENT_OCCLUSION_INDEX = 7; // float

        public static final int COLOR_INDEX = 8; // vec4

        public final VertexResource buffer;
        public final IndexResource indices = new IndexResource();

        public final VertexAttributeBinding<Vector3fc, Vector3f> position;
        public final VertexAttributeBinding<Vector3fc, Vector3f> normals;
        public final VertexAttributeBinding<Vector2fc, Vector2f> uv0;

        public final VertexAttributeBinding<Colorc, Color> color;

        public final VertexByteAttributeBinding flags;
        public final VertexByteAttributeBinding frames;

        public final VertexFloatAttributeBinding sunlight;         // this could be changed to a single byte
        public final VertexFloatAttributeBinding blockLight;       // this could be changed to a single byte
        public final VertexFloatAttributeBinding ambientOcclusion; // this could be changed to a single byte
        public int vertexCount;


        VertexElements() {
            VertexResourceBuilder builder = new VertexResourceBuilder();
            position = builder.add(VERTEX_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
            normals = builder.add(NORMAL_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
            uv0 = builder.add(UV0_INDEX, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);

            flags = builder.add(FLAGS_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);
            frames = builder.add(FRAME_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);

            sunlight = builder.add(SUNLIGHT_INDEX, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);
            blockLight = builder.add(BLOCK_INDEX, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);
            ambientOcclusion = builder.add(AMBIENT_OCCLUSION_INDEX, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);

            color = builder.add(COLOR_INDEX, GLAttributes.COLOR_4_F_VERTEX_ATTRIBUTE);

            buffer = builder.build();
        }
    }
}

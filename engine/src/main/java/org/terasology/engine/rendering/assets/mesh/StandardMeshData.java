// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resouce.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexAttribute;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexFloatAttribute;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

public class StandardMeshData extends MeshData {
    public static final int VERTEX_INDEX = 0;
    public static final int NORMAL_INDEX = 1;
    public static final int UV0_INDEX = 2;
    public static final int COLOR0_INDEX = 3;
    public static final int LIGHT0_INDEX = 4;

    private final int vertexCount;

    public final VertexResource positionBuffer;
    public final VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> position;

    public final VertexResource normalBuffer;
    public final VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> normal;


    public final VertexResource uvBuffer;
    public final VertexFloatAttribute.VertexAttributeFloatBinding<Vector2f> uv0;


    public final VertexResource colorBuffer;
    public final VertexFloatAttribute.VertexAttributeFloatBinding<Color> color0;

    public final VertexResource lightBuffer;
    public final VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> light0;

    public final IndexResource indices;

    public StandardMeshData(int size, int indices) {
        this.vertexCount = size;

        VertexResource.VertexResourceBuilder builder = new VertexResource.VertexResourceBuilder(size);
        position = builder.add(VERTEX_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, true);
        positionBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        normal = builder.add(NORMAL_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, true);
        normalBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        uv0 = builder.add(UV0_INDEX, VertexAttribute.VECTOR_2_F_VERTEX_ATTRIBUTE, false);
        uvBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        color0 = builder.add(COLOR0_INDEX, VertexAttribute.COLOR_4_F_VERTEX_ATTRIBUTE, false);
        lightBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        light0 = builder.add(LIGHT0_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, false);
        colorBuffer = builder.build();

        this.indices = new IndexResource(indices, true);
    }

    @Override
    public Vector3f[] getVertices() {
        return position.getStore();
    }

    @Override
    public VertexResource[] getVertexResource() {
        return new VertexResource[]{
                positionBuffer,
                normalBuffer,
                uvBuffer,
                colorBuffer,
                lightBuffer
        };
    }

    @Override
    public IndexResource getIndexResource() {
        return indices;
    }

    @Override
    public int vertexCount() {
        return vertexCount;
    }
}

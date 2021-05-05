// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.terasology.engine.rendering.assets.mesh.resouce.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexAttribute;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexAttributeFloatBinding;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;
import org.terasology.nui.Color;

public class StandardMeshData extends MeshData {
    public static final int VERTEX_INDEX = 0;
    public static final int NORMAL_INDEX = 1;
    public static final int UV0_INDEX = 2;
    public static final int UV1_INDEX = 3;
    public static final int COLOR0_INDEX = 4;
    public static final int LIGHT0_INDEX = 5;

    public final VertexResource positionBuffer;
    public final VertexAttributeFloatBinding<Vector3f> position;

    public final VertexResource normalBuffer;
    public final VertexAttributeFloatBinding<Vector3f> normal;

    public final VertexResource uv0Buffer;
    public final VertexAttributeFloatBinding<Vector2f> uv0;

    public final VertexResource uv1Buffer;
    public final VertexAttributeFloatBinding<Vector2f> uv1;

    public final VertexResource colorBuffer;
    public final VertexAttributeFloatBinding<Color> color0;

    public final VertexResource lightBuffer;
    public final VertexAttributeFloatBinding<Vector3f> light0;

    public final IndexResource indices;

    public StandardMeshData(int size, int indices) {

        VertexResource.VertexResourceBuilder builder = new VertexResource.VertexResourceBuilder(size);
        position = builder.add(VERTEX_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, true);
        positionBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        normal = builder.add(NORMAL_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, true);
        normalBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        uv0 = builder.add(UV0_INDEX, VertexAttribute.VECTOR_2_F_VERTEX_ATTRIBUTE, false);
        uv0Buffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        uv1 = builder.add(UV1_INDEX, VertexAttribute.VECTOR_2_F_VERTEX_ATTRIBUTE, false);
        uv1Buffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        color0 = builder.add(COLOR0_INDEX, VertexAttribute.COLOR_4_F_VERTEX_ATTRIBUTE, false);
        lightBuffer = builder.build();

        builder = new VertexResource.VertexResourceBuilder(size);
        light0 = builder.add(LIGHT0_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, false);
        colorBuffer = builder.build();

        this.indices = new IndexResource(indices, true);
    }

    public StandardMeshData(
            VertexAttributeFloatBinding<Vector3f> position,
            VertexAttributeFloatBinding<Vector3f> normal,
            VertexAttributeFloatBinding<Vector2f> uv0,
            VertexAttributeFloatBinding<Vector2f> uv1,
            VertexAttributeFloatBinding<Color> color0,
            VertexAttributeFloatBinding<Vector3f> light0,
            IndexResource indexResource) {
        this.position = position;
        this.positionBuffer = position.getResource();

        this.normal = normal;
        this.normalBuffer = normal.getResource();

        this.uv0 = uv0;
        this.uv0Buffer = uv0.getResource();

        this.uv1 = uv1;
        this.uv1Buffer = uv1.getResource();

        this.color0 = color0;
        this.colorBuffer = color0.getResource();

        this.light0 = light0;
        this.lightBuffer = light0.getResource();

        this.indices= indexResource;

    }


    @Override
    public float[] verts() {
        return position.getStore();
    }

    @Override
    public VertexResource[] vertexResources() {
        return new VertexResource[]{
                positionBuffer,
                normalBuffer,
                uv0Buffer,
                uv1Buffer,
                colorBuffer,
                lightBuffer
        };
    }

    @Override
    public IndexResource indexResource() {
        return indices;
    }
}

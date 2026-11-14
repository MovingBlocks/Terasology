// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;

import java.util.List;

public class StandardSkinnedMeshData extends SkinnedMeshData {

    public static final int VERTEX_INDEX = 0;
    public static final int NORMAL_INDEX = 1;
    public static final int UV0_INDEX = 2;
    public static final int UV1_INDEX = 3;

    public static final int BONE_IDX0_INDEX = 4;
    public static final int BONE_IDX1_INDEX = 5;
    public static final int BONE_IDX2_INDEX = 6;
    public static final int BONE_IDX3_INDEX = 7;
    public static final int BONE_WEIGHT_INDEX = 8;

    public final VertexResource positionBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> position;

    public final VertexResource normalBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> normal;

    public final VertexResource uv0Buffer;
    public final VertexAttributeBinding<Vector2fc, Vector2f> uv0;

    public final VertexResource uv1Buffer;
    public final VertexAttributeBinding<Vector2fc, Vector2f> uv1;

    public final VertexResource boneIndexBuffer0;
    public final VertexByteAttributeBinding boneIndex0;

    public final VertexResource boneIndexBuffer1;
    public final VertexByteAttributeBinding boneIndex1;

    public final VertexResource boneIndexBuffer2;
    public final VertexByteAttributeBinding boneIndex2;

    public final VertexResource boneIndexBuffer3;
    public final VertexByteAttributeBinding boneIndex3;

    public final VertexResource weightBuffer;
    public final VertexAttributeBinding<Vector4fc, Vector4f> weight;

    public final IndexResource indices;

    private final List<Bone> bones;

    public StandardSkinnedMeshData(List<Bone> bones) {
        this(DrawingMode.TRIANGLES, AllocationType.STATIC, bones);
    }

    public StandardSkinnedMeshData(DrawingMode mode, AllocationType allocationType, List<Bone> bones) {
        super(mode, allocationType);
        this.bones = bones;

        VertexResourceBuilder builder = new VertexResourceBuilder();
        position = builder.add(VERTEX_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        positionBuffer = builder.build();

        builder = new VertexResourceBuilder();
        normal = builder.add(NORMAL_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        normalBuffer = builder.build();

        builder = new VertexResourceBuilder();
        uv0 = builder.add(UV0_INDEX, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);
        uv0Buffer = builder.build();

        builder = new VertexResourceBuilder();
        uv1 = builder.add(UV1_INDEX, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);
        uv1Buffer = builder.build();

        builder = new VertexResourceBuilder();
        boneIndex0 = builder.add(BONE_IDX0_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE, VertexResource.FEATURE_INTEGER);
        boneIndexBuffer0 = builder.build();

        builder = new VertexResourceBuilder();
        boneIndex1 = builder.add(BONE_IDX1_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE, VertexResource.FEATURE_INTEGER);
        boneIndexBuffer1 = builder.build();

        builder = new VertexResourceBuilder();
        boneIndex2 = builder.add(BONE_IDX2_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE, VertexResource.FEATURE_INTEGER);
        boneIndexBuffer2 = builder.build();

        builder = new VertexResourceBuilder();
        boneIndex3 = builder.add(BONE_IDX3_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE, VertexResource.FEATURE_INTEGER);
        boneIndexBuffer3 = builder.build();

        builder = new VertexResourceBuilder();
        weight = builder.add(BONE_WEIGHT_INDEX, GLAttributes.VECTOR_4_F_VERTEX_ATTRIBUTE);
        weightBuffer = builder.build();

        this.indices = new IndexResource();
    }

    @Override
    public VertexByteAttributeBinding boneIndex0() {
        return boneIndex0;
    }

    @Override
    public VertexByteAttributeBinding boneIndex1() {
        return boneIndex1;
    }

    @Override
    public VertexByteAttributeBinding boneIndex2() {
        return boneIndex2;
    }

    @Override
    public VertexByteAttributeBinding boneIndex3() {
        return boneIndex3;
    }

    @Override
    public VertexAttributeBinding<Vector4fc, Vector4f> weight() {
        return weight;
    }

    @Override
    public List<Bone> bones() {
        return this.bones;
    }

    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> positions() {
        return position;
    }

    @Override
    public VertexResource[] vertexResources() {
        return new VertexResource[] {
                positionBuffer,
                normalBuffer,
                uv0Buffer,
                uv1Buffer,
                boneIndexBuffer0,
                boneIndexBuffer1,
                boneIndexBuffer2,
                boneIndexBuffer3,
                weightBuffer
        };
    }

    @Override
    public IndexResource indexResource() {
        return indices;
    }
}

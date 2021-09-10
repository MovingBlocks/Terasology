// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

public class StandardMeshData extends MeshData {
    public static final int VERTEX_INDEX = 0;
    public static final int NORMAL_INDEX = 1;
    public static final int UV0_INDEX = 2;
    public static final int UV1_INDEX = 3;
    public static final int COLOR0_INDEX = 4;
    public static final int LIGHT0_INDEX = 5;

    public final VertexResource positionBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> position;

    public final VertexResource normalBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> normal;

    public final VertexResource uv0Buffer;
    public final VertexAttributeBinding<Vector2fc, Vector2f> uv0;

    public final VertexResource uv1Buffer;
    public final VertexAttributeBinding<Vector2fc, Vector2f> uv1;

    public final VertexResource colorBuffer;
    public final VertexAttributeBinding<Colorc, Color> color0;

    public final VertexResource lightBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> light0;

    public final IndexResource indices;


    /**
     * transfer buffered data to another {@link StandardMeshData}
     *
     * @param data the data
     */
    public StandardMeshData(StandardMeshData data) {
        this();
        positionBuffer.copy(data.positionBuffer);
        normalBuffer.copy(data.normalBuffer);
        uv0Buffer.copy(data.uv0Buffer);
        uv1Buffer.copy(data.uv1Buffer);
        lightBuffer.copy(data.lightBuffer);
        colorBuffer.copy(data.colorBuffer);
        indices.copy(data.indices);

        position.setPosition(data.position.getPosition());
        normal.setPosition(data.normal.getPosition());
        uv0.setPosition(data.uv0.getPosition());
        uv1.setPosition(data.uv1.getPosition());
        color0.setPosition(data.color0.getPosition());
        light0.setPosition(data.light0.getPosition());
    }

    public StandardMeshData() {
        this(DrawingMode.TRIANGLES, AllocationType.STATIC);
    }

    public StandardMeshData(DrawingMode mode, AllocationType allocationType) {
        super(mode, allocationType);

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
        color0 = builder.add(COLOR0_INDEX, GLAttributes.COLOR_4_F_VERTEX_ATTRIBUTE);
        colorBuffer = builder.build();

        builder = new VertexResourceBuilder();
        light0 = builder.add(LIGHT0_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        lightBuffer = builder.build();

        this.indices = new IndexResource();
    }

    public void reserve(int numVertices, int numIndices) {
        positionBuffer.reserveElements(numVertices);
        normalBuffer.reserveElements(numVertices);
        uv0Buffer.reserveElements(numVertices);
        uv1Buffer.reserveElements(numVertices);
        lightBuffer.reserveElements(numVertices);
        colorBuffer.reserveElements(numVertices);
        indices.reserveElements(numIndices);
    }

    public void reallocate(int numVerts, int numIndices) {
        positionBuffer.allocateElements(numVerts);
        normalBuffer.allocateElements(numVerts);
        uv0Buffer.allocateElements(numVerts);
        uv1Buffer.allocateElements(numVerts);
        lightBuffer.allocateElements(numVerts);
        colorBuffer.allocateElements(numVerts);
        indices.allocateElements(numIndices);
    }


    /**
     * append mesh data to current mesh
     * @param transform transformation to apply to target
     * @param target target mesh
     */
    public void combine(Matrix4f transform, StandardMeshData target) {
        int start = this.position.getPosition();
        if (!position.isEmpty() || !target.position.isEmpty()) {
            position.setPosition(start);
            Vector3f temp = new Vector3f();
            for (int i = 0; i < target.position.elements(); i++) {
                this.position.put(transform.transformPosition(target.position.get(i, temp)));
            }
        }
        if (!normal.isEmpty() || !target.normal.isEmpty()) {
            VertexAttributeBinding.copy(target.normal, start, this.normal, new Vector3f());
        }
        if (!uv0.isEmpty() || !target.uv0.isEmpty()) {
            VertexAttributeBinding.copy(target.uv0, start, this.uv0, new Vector2f());
        }
        if (!uv1.isEmpty() || !target.uv1.isEmpty()) {
            VertexAttributeBinding.copy(target.uv1, start, this.uv1, new Vector2f());
        }
        if (!color0.isEmpty() || !target.color0.isEmpty()) {
            VertexAttributeBinding.copy(target.color0, start, this.color0, new Color());
        }
        if (!light0.isEmpty() || !target.light0.isEmpty()) {
            VertexAttributeBinding.copy(target.light0, start, this.light0, new Vector3f());
        }
    }


    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> positions() {
        return position;
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

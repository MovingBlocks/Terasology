// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.gltf;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexFloatAttribute;
import org.terasology.engine.rendering.gltf.model.GLTFAccessor;
import org.terasology.engine.rendering.gltf.model.GLTFBufferView;
import org.terasology.engine.rendering.gltf.model.GLTFComponentType;
import org.terasology.nui.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GLTFAttributeMapping {
    private GLTFAttributeMapping() {

    }


    public static void readVec3FBuffer(byte[] buffer, GLTFAccessor accessor, GLTFBufferView bufferView, VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> mapping) {
        if (accessor.getComponentType() != GLTFComponentType.FLOAT) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, bufferView.getByteOffset() + accessor.getByteOffset(), bufferView.getByteLength() - accessor.getByteOffset());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int gap = 0;
        if (bufferView.getByteStride() > 0) {
            gap = bufferView.getByteStride() - accessor.getComponentType().getByteLength() * accessor.getType().getDimension();
        }
        Vector3f pos = new Vector3f();
        if (byteBuffer.position() < byteBuffer.limit()) {
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                pos.setComponent(i, byteBuffer.getFloat());
            }
            mapping.put(pos);
        }
        while (byteBuffer.position() < byteBuffer.limit() - gap) {
            byteBuffer.position(byteBuffer.position() + gap);
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                pos.setComponent(i, byteBuffer.getFloat());
            }
            mapping.put(pos);
        }
    }


    public static void readVec2FBuffer(byte[] buffer, GLTFAccessor accessor, GLTFBufferView bufferView, VertexFloatAttribute.VertexAttributeFloatBinding<Vector2f> mapping) {
        if (accessor.getComponentType() != GLTFComponentType.FLOAT) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, bufferView.getByteOffset() + accessor.getByteOffset(), bufferView.getByteLength() - accessor.getByteOffset());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int gap = 0;
        if (bufferView.getByteStride() > 0) {
            gap = bufferView.getByteStride() - accessor.getComponentType().getByteLength() * accessor.getType().getDimension();
        }
        Vector2f pos = new Vector2f();
        if (byteBuffer.position() < byteBuffer.limit()) {
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                pos.setComponent(i, byteBuffer.getFloat());
            }
            mapping.put(pos);
        }
        while (byteBuffer.position() < byteBuffer.limit() - gap) {
            byteBuffer.position(byteBuffer.position() + gap);
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                pos.setComponent(i, byteBuffer.getFloat());
            }
            mapping.put(pos);
        }
    }


    public static void readVec4FBuffer(byte[] buffer, GLTFAccessor accessor, GLTFBufferView bufferView, VertexFloatAttribute.VertexAttributeFloatBinding<Vector4f> mapping) {
        if (accessor.getComponentType() != GLTFComponentType.FLOAT) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, bufferView.getByteOffset() + accessor.getByteOffset(), bufferView.getByteLength() - accessor.getByteOffset());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int gap = 0;
        if (bufferView.getByteStride() > 0) {
            gap = bufferView.getByteStride() - accessor.getComponentType().getByteLength() * accessor.getType().getDimension();
        }
        Vector4f pos = new Vector4f();
        if (byteBuffer.position() < byteBuffer.limit()) {
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                pos.setComponent(i, byteBuffer.getFloat());
            }
            mapping.put(pos);
        }
        while (byteBuffer.position() < byteBuffer.limit() - gap) {
            byteBuffer.position(byteBuffer.position() + gap);
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                pos.setComponent(i, byteBuffer.getFloat());
            }
            mapping.put(pos);
        }
    }

    public static void readColor4FBuffer(byte[] buffer, GLTFAccessor accessor, GLTFBufferView bufferView, VertexFloatAttribute.VertexAttributeFloatBinding<Color> mapping) {
        if (accessor.getComponentType() != GLTFComponentType.FLOAT) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, bufferView.getByteOffset() + accessor.getByteOffset(), bufferView.getByteLength() - accessor.getByteOffset());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int gap = 0;
        if (bufferView.getByteStride() > 0) {
            gap = bufferView.getByteStride() - accessor.getComponentType().getByteLength() * accessor.getType().getDimension();
        }
        Vector4f value = new Vector4f();
        Color c = new Color();
        if (byteBuffer.position() < byteBuffer.limit()) {
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                value.setComponent(i, byteBuffer.getFloat());
            }
            c.set(value);
            mapping.put(c);
        }
        while (byteBuffer.position() < byteBuffer.limit() - gap) {
            byteBuffer.position(byteBuffer.position() + gap);
            for (int i = 0; i < accessor.getType().getDimension(); i++) {
                value.setComponent(i, byteBuffer.getFloat());
            }
            c.set(value);
            mapping.put(c);
        }
    }
}

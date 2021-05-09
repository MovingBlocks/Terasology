// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.terasology.nui.Color;

import java.nio.ByteBuffer;

/**
 * attribute maps a target object or a primitive data to a {@link VertexResource}
 *
 * @param <TARGET> the target object
 */
public class VertexAttribute<TARGET> {


    public final TypeMapping mapping;
    public final int count;
    public final Class<TARGET> type;
    public final AttributeConfiguration<TARGET> configuration;

    public interface AttributeConfiguration<TARGET> {
        void write(TARGET value, int vertIdx, int offset, VertexResource resource);
        TARGET read(int vertIdx, int offset, VertexResource resource, TARGET dest);
        int size(int vertIdx, int offset, VertexResource resource);
        int numElements(int offset, VertexResource resource);
    }

    /**
     * @param type the mapping type
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexAttribute(Class<TARGET> type, AttributeConfiguration<TARGET> attributeConfiguration, TypeMapping mapping, int count) {
        this.type = type;
        this.mapping = mapping;
        this.count = count;
        this.configuration = attributeConfiguration;
    }


    public enum TypeMapping {
        ATTR_FLOAT(Float.BYTES, GL30.GL_FLOAT),
        ATTR_SHORT(Short.BYTES, GL30.GL_SHORT),
        ATTR_BYTE(Byte.BYTES, GL30.GL_BYTE),
        ATTR_INT(Integer.BYTES, GL30.GL_INT);

        public final int size;
        public final int glType;

        TypeMapping(int size, int glType) {
            this.size = size;
            this.glType = glType;
        }
    }



}

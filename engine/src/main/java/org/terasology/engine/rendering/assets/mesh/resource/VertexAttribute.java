// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.opengl.GL30;

/**
 * attribute maps a target object or a primitive data to a {@link VertexResource}
 *
 * @param <T> the target object
 */
public class VertexAttribute<T, TImpl extends T> {

    public final TypeMapping mapping;
    public final int count;
    public final Class<TImpl> type;
    public final AttributeConfiguration<T, TImpl> configuration;

    public interface AttributeConfiguration<T, TImpl> {
        void write(T value, int vertIdx, int offset, VertexResource resource);

        TImpl read(int vertIdx, int offset, VertexResource resource, TImpl dest);

        int size(int vertIdx, int offset, VertexResource resource);

        int numElements(int offset, VertexResource resource);
    }

    /**
     * @param type the mapping type
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexAttribute(Class<TImpl> type, AttributeConfiguration<T, TImpl> attributeConfiguration, TypeMapping mapping, int count) {
        this.type = type;
        this.mapping = mapping;
        this.count = count;
        this.configuration = attributeConfiguration;
    }
}

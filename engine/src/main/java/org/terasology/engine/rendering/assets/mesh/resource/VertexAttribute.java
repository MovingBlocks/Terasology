// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

/**
 * attribute maps a target object or a primitive data to a {@link VertexResource}
 *
 * @param <T> the target object type
 * @param <I> a class implementing the target object type
 */
public class VertexAttribute<T, I extends T> extends BaseVertexAttribute {

    public final Class<I> type;
    public final AttributeConfiguration<T, I> configuration;

    public interface AttributeConfiguration<U, V> {
        void write(U value, int vertIdx, int offset, VertexResource resource);

        V read(int vertIdx, int offset, VertexResource resource, V dest);
    }

    /**
     * @param type the mapping type
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexAttribute(Class<I> type, AttributeConfiguration<T, I> attributeConfiguration, TypeMapping mapping, int count) {
        super(mapping, count);
        this.type = type;
        this.configuration = attributeConfiguration;
    }
}

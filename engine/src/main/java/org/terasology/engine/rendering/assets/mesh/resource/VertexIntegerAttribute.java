// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexIntegerAttribute extends BaseVertexAttribute {
    public final VertexIntegerAttribute.AttributeConfiguration configuration;

    /**
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexIntegerAttribute(VertexIntegerAttribute.AttributeConfiguration attributeConfiguration,
                                     TypeMapping mapping, int count) {
        super(mapping, count);
        this.configuration = attributeConfiguration;
    }

    public interface AttributeConfiguration {
        void write(int value, int vertIdx, int offset, VertexResource resource);

        int read(int vertIdx, int offset, VertexResource resource);
    }
}

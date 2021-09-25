// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexByteAttribute extends BaseVertexAttribute {
    public final VertexByteAttribute.AttributeConfiguration configuration;

    /**
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexByteAttribute(VertexByteAttribute.AttributeConfiguration attributeConfiguration,
                                  TypeMapping mapping, int count) {
        super(mapping, count);
        this.configuration = attributeConfiguration;
    }

    public interface AttributeConfiguration {
        void write(byte value, int vertIdx, int offset, VertexResource resource);

        byte read(int vertIdx, int offset, VertexResource resource);
    }
}

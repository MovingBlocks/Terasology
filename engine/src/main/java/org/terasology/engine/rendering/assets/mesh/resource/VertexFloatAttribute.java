// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexFloatAttribute extends BaseVertexAttribute {
    public final VertexFloatAttribute.AttributeConfiguration configuration;

    public interface AttributeConfiguration {
        void write(float value, int vertIdx, int offset, VertexResource resource);

        float read(int vertIdx, int offset, VertexResource resource);
    }
    /**
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexFloatAttribute(VertexFloatAttribute.AttributeConfiguration attributeConfiguration,
                                     TypeMapping mapping, int count) {
        super(mapping, count);
        this.configuration = attributeConfiguration;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import java.nio.ByteBuffer;
import java.util.Optional;

public class VertexFloatAttribute<TARGET> extends VertexAttribute<TARGET> {

    public static VertexAttributeFloatBinding EMPTY_BINDING = new VertexAttributeFloatBinding(null, VECTOR_3_F_VERTEX_ATTRIBUTE, 0);

    public interface AttributeConfiguration<TARGET> {
        void map(TARGET value, int vertIdx, int stride, int offset, ByteBuffer buffer, Optional<float[]> store);

        void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer, Optional<float[]> store);


    }
    public final VertexFloatAttribute.AttributeConfiguration<TARGET> configuration;
    protected VertexFloatAttribute(Class<TARGET> type,VertexFloatAttribute.AttributeConfiguration<TARGET> configuration,
                                VertexAttribute.TypeMapping mapping, int count) {
        super(type, mapping, count);
        this.configuration = configuration;
    }

}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import java.util.ArrayList;
import java.util.List;

public class VertexResourceBuilder {
    private List<VertexResource.VertexDefinition> definitions = new ArrayList<>();
    private int inStride;
    private VertexResource resource = new VertexResource();

    public VertexResourceBuilder() {
    }

    /**
     * add an attribute and provides an {@link VertexAttributeBinding}
     *
     * @param location the index of the attribute binding
     * @param attribute the attribute that describes the binding
     * @param <TARGET>
     * @return
     */
    public <TARGET> VertexAttributeBinding<TARGET> add(int location, VertexAttribute<TARGET> attribute) {
        VertexAttributeBinding<TARGET> result = new VertexAttributeBinding<TARGET>(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexResource build() {
        resource.setDefinitions(definitions.toArray(new VertexResource.VertexDefinition[]{}));
        resource.allocate(0, inStride);
        return resource;
    }
}

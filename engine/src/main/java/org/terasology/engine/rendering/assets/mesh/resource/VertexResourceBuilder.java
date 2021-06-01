// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import java.util.ArrayList;
import java.util.List;

/**
 * factory that is used to define an attribute binding to the backing {@link VertexResource}
 */
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
     * @param <T>
     * @return
     */
    public <T, TImpl extends T> VertexAttributeBinding<T, TImpl> add(int location, VertexAttribute<T, TImpl> attribute) {
        VertexAttributeBinding<T, TImpl> result = new VertexAttributeBinding<T, TImpl>(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexIntegerAttributeBinding add(int location, VertexIntegerAttribute attribute) {
        VertexIntegerAttributeBinding result = new VertexIntegerAttributeBinding(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexFloatAttributeBinding add(int location, VertexFloatAttribute attribute) {
        VertexFloatAttributeBinding result = new VertexFloatAttributeBinding(resource, inStride, attribute);
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

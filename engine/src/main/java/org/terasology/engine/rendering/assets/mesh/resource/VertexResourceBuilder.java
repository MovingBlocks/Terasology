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
     * @param <T> the target object type
     * @param <I> a class implementing the target object type
     */
    public <T, I extends T> VertexAttributeBinding<T, I> add(int location, VertexAttribute<T, I> attribute, int feature) {
        VertexAttributeBinding<T, I> result = new VertexAttributeBinding<T, I>(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute, feature));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public <T, I extends T> VertexAttributeBinding<T, I> add(int location, VertexAttribute<T, I> attribute) {
        return add(location, attribute, 0);
    }

    public VertexIntegerAttributeBinding add(int location, VertexIntegerAttribute attribute, int feature) {
        VertexIntegerAttributeBinding result = new VertexIntegerAttributeBinding(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute, feature));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexIntegerAttributeBinding add(int location, VertexIntegerAttribute attribute) {
        return add(location, attribute, 0);
    }

    public VertexFloatAttributeBinding add(int location, VertexFloatAttribute attribute, int features) {
        VertexFloatAttributeBinding result = new VertexFloatAttributeBinding(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute, features));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexFloatAttributeBinding add(int location, VertexFloatAttribute attribute) {
        return add(location, attribute, 0);
    }

    public VertexByteAttributeBinding add(int location, VertexByteAttribute attribute, int features) {
        VertexByteAttributeBinding result = new VertexByteAttributeBinding(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute, features));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexByteAttributeBinding add(int location, VertexByteAttribute attribute) {
        return add(location, attribute, 0);
    }

    public VertexShortAttributeBinding add(int location, VertexShortAttribute attribute, int features) {
        VertexShortAttributeBinding result = new VertexShortAttributeBinding(resource, inStride, attribute);
        this.definitions.add(new VertexResource.VertexDefinition(location, inStride, attribute, features));
        inStride += attribute.mapping.size * attribute.count;
        return result;
    }

    public VertexShortAttributeBinding add(int location, VertexShortAttribute attribute) {
        return add(location, attribute, 0);
    }

    public VertexResource build() {
        resource.setDefinitions(definitions.toArray(new VertexResource.VertexDefinition[]{}));
        resource.allocate(0, inStride);
        return resource;
    }
}

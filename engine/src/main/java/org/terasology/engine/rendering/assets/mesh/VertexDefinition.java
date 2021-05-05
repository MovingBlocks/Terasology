// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.terasology.engine.rendering.assets.mesh.resouce.VertexAttribute;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;

/**
 *
 */
public class VertexDefinition {
    public int location;
    public VertexResource resource;
    public VertexAttribute attribute;
    public int offset;
    public int stride;
    public int elements;

    public VertexDefinition(int location, int stride, VertexAttribute attribute) {
        this.location = location;
        this.attribute = attribute;
    }
}

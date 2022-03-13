// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class BaseVertexAttribute {

    public final TypeMapping mapping;
    public final int count;

    protected BaseVertexAttribute(TypeMapping mapping, int count) {
        this.mapping = mapping;
        this.count = count;
    }

}

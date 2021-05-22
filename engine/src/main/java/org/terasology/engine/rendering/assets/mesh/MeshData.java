// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.gestalt.assets.AssetData;

/**
 */
public abstract class MeshData implements AssetData {
    private final DrawingMode mode;

    public MeshData() {
        this(DrawingMode.TRIANGLES);
    }

    public MeshData(DrawingMode mode) {
        this.mode = mode;
    }

    public DrawingMode getMode() {
        return mode;
    }

    public abstract VertexAttributeBinding<Vector3fc, Vector3f> positions();
    public abstract VertexResource[] vertexResources();
    public abstract IndexResource indexResource();
}

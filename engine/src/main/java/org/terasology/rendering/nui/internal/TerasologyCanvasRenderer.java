// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.internal;

import org.joml.Quaternionfc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.opengl.FrameBufferObject;

/**
 */
public interface TerasologyCanvasRenderer extends CanvasRenderer {
    FrameBufferObject getFBO(ResourceUrn urn, Vector2ic size);

    void drawMesh(Mesh mesh, Material material, Rectanglei drawRegion, Rectanglei cropRegion, Quaternionfc rotation, Vector3fc offset, float scale, float alpha);

    void drawMaterialAt(Material material, Rectanglei drawRegion);
}

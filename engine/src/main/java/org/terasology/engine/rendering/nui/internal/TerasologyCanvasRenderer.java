// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.internal;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.opengl.FrameBufferObject;

/**
 */
public interface TerasologyCanvasRenderer extends CanvasRenderer {
    FrameBufferObject getFBO(ResourceUrn urn, BaseVector2i size);

    void drawMesh(Mesh mesh, Material material, Rect2i drawRegion, Rect2i cropRegion, Quat4f rotation, Vector3f offset, float scale, float alpha);

    void drawMaterialAt(Material material, Rect2i drawRegion);
}

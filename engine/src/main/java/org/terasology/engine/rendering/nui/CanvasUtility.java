// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.joml.Quaternionfc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasImpl;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.SubRegion;

// HACK: This whole class was created in order to provide access to internal, implementation specific methods.

/**
 * Contains some methods not available within the standard {@link Canvas} interface, such as drawing meshes.
 */
public final class CanvasUtility {
    private CanvasUtility() {
    }

    public static void drawMesh(Canvas canvas, Mesh mesh, Texture texture, Rectanglei region, Quaternionfc rotation, Vector3fc offset, float scale) {
        // TODO: Find a way to abstractly implement drawMesh in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing meshes is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMesh(mesh, texture, region, rotation, offset, scale);
    }

    public static void drawMesh(Canvas canvas, Mesh mesh, Material material, Rectanglei region, Quaternionfc rotation, Vector3fc offset, float scale) {
        // TODO: Find a way to abstractly implement drawMesh in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing meshes is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMesh(mesh, material, region, rotation, offset, scale);
    }

    public static void drawMaterial(Canvas canvas, Material material, Rectanglei region) {
        // TODO: Find a way to abstractly implement drawMaterial in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing materials is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMaterial(material, region);
    }

    public static SubRegion subRegionFBO(Canvas canvas, ResourceUrn uri, Vector2ic size) {
        // TODO: Find a way to abstractly implement subRegionFBO in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Used FBO sub-regions is only supported using Terasology's internal renderer.");
        }

        return ((TerasologyCanvasImpl) canvas).subRegionFBO(uri, size);
    }
}

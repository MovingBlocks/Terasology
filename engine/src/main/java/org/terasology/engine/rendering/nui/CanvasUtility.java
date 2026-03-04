// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.joml.Quaternionfc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.font.Font;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasImpl;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.Colorc;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.SubRegion;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.VerticalAlign;

// HACK: This whole class was created in order to provide access to internal, implementation specific methods.

/**
 * Contains some methods not available within the standard {@link Canvas} interface, such as drawing meshes.
 */
public final class CanvasUtility {
    private CanvasUtility() {
    }

    /**
     * Draws a texture with rotation support.
     * This is a convenience method that wraps drawMesh with rotation using a billboard mesh.
     *
     * @param canvas the canvas to draw on
     * @param texture the texture to draw
     * @param region the region to draw in
     * @param rotation the rotation to apply (as quaternion)
     * @param offset the offset from the center of the region
     * @param scale the scale factor
     */
    public static void drawTexture(Canvas canvas, UITextureRegion texture, Rectanglei region, Quaternionfc rotation,
                                   Vector3fc offset, float scale) {
        drawTexture(canvas, texture, region, rotation, offset, scale, 1.0f);
    }

    /**
     * Draws a texture with rotation and alpha support.
     * This is a convenience method that wraps drawMesh with rotation using a billboard mesh.
     *
     * @param canvas the canvas to draw on
     * @param texture the texture to draw
     * @param region the region to draw in
     * @param rotation the rotation to apply (as quaternion)
     * @param offset the offset from the center of the region
     * @param scale the scale factor
     * @param alpha the alpha value (0-1)
     */
    public static void drawTexture(Canvas canvas, UITextureRegion texture, Rectanglei region, Quaternionfc rotation,
                                   Vector3fc offset, float scale, float alpha) {
        // TODO: Find a way to abstractly implement drawTexture with rotation in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing rotated textures is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawTexture(texture, region, rotation, offset, scale, alpha);
    }

    public static void drawMesh(Canvas canvas, Mesh mesh, Texture texture, Rectanglei region, Quaternionfc rotation,
                                Vector3fc offset, float scale) {
        // TODO: Find a way to abstractly implement drawMesh in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing meshes is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMesh(mesh, texture, region, rotation, offset, scale);
    }

    public static void drawMesh(Canvas canvas, Mesh mesh, Material material, Rectanglei region, Quaternionfc rotation,
                                Vector3fc offset, float scale) {
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

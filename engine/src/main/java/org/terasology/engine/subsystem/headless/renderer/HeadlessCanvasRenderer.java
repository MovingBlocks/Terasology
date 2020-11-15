// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem.headless.renderer;

import org.joml.Quaternionfc;
import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.assets.ResourceUrn;
import org.terasology.nui.Border;
import org.terasology.nui.Colorc;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.VerticalAlign;
import org.terasology.nui.asset.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.rendering.opengl.FrameBufferObject;

public class HeadlessCanvasRenderer implements TerasologyCanvasRenderer {

    @Override
    public void preRender() {
        // Do nothing
    }

    @Override
    public void postRender() {
        // Do nothing
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rectanglei drawRegion, Rectanglei cropRegion, Quaternionfc rotation, Vector3fc offset, float scale, float alpha) {
        // Do nothing
    }

    @Override
    public Vector2i getTargetSize() {
        return new Vector2i();
    }

    @Override
    public void drawMaterialAt(Material material, Rectanglei drawRegion) {
        // Do nothing
    }

    @Override
    public void drawLine(int sx, int sy, int ex, int ey, Colorc color) {
        // Do nothing
    }

    @Override
    public void crop(Rectanglei cropRegion) {
        // Do nothing
    }

    @Override
    public FrameBufferObject getFBO(ResourceUrn uri, Vector2ic region) {
        return null;
    }

    @Override
    public void drawTexture(UITextureRegion texture, Colorc color, ScaleMode mode, Rectanglei absoluteRegion, float ux, float uy, float uw, float uh, float alpha) {
        // Do nothing
    }

    @Override
    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rectanglei absoluteRegion, Colorc color, Colorc shadowColor,
                         float alpha, boolean underlined) {
        // Do nothing
    }

    @Override
    public void drawTextureBordered(UITextureRegion texture, Rectanglei absoluteRegion, Border border, boolean tile, float ux, float uy, float uw, float uh, float alpha) {
        // Do nothing
    }

    @Override
    public void setUiScale(float uiScale) {
        // Do nothing
    }
}

// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.internal;

import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.opengl.FrameBufferObject;
import org.terasology.engine.rendering.opengl.WgpuTexture;
import org.terasology.engine.rust.EngineKernel;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Border;
import org.terasology.nui.Colorc;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.VerticalAlign;
import org.terasology.nui.asset.font.Font;

public class WgpuCanvasRenderer implements TerasologyCanvasRenderer {
    private DisplayDevice displayDevice;

    public WgpuCanvasRenderer(Context context) {
        this.displayDevice = context.get(DisplayDevice.class);
    }

    @Override
    public void preRender() {

    }

    @Override
    public void postRender() {

    }

    @Override
    public Vector2i getTargetSize() {
        return new Vector2i(displayDevice.getWidth(), displayDevice.getHeight());
    }

    @Override
    public void crop(Rectanglei cropRegion) {
        EngineKernel kernel = EngineKernel.instance();

    }

    @Override
    public void drawLine(int sx, int sy, int ex, int ey, Colorc color) {

    }

    @Override
    public void drawTexture(UITextureRegion texture, Colorc color, ScaleMode mode, Rectanglei absoluteRegion, float ux, float uy, float uw, float uh, float alpha) {
        EngineKernel kernel = EngineKernel.instance();
        if(texture instanceof WgpuTexture) {
            Vector2fc size = ((WgpuTexture) texture).getTeraTexture().getSize();

            kernel.cmdUIDrawTexture(
                    ((WgpuTexture) texture).getTeraTexture(),
                    new Rectanglef(ux, uy , ux + uw, uy + uh),
                    new Rectanglef(absoluteRegion.minX(), absoluteRegion.minY(), absoluteRegion.maxX(), absoluteRegion.maxY()));
//            ((WgpuTexture) texture).getTexture()
        }

    }

    @Override
    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rectanglei absoluteRegion, Colorc color, Colorc shadowColor, float alpha, boolean underlined) {

    }

    @Override
    public void drawTextureBordered(UITextureRegion texture, Rectanglei absoluteRegion, Border border, boolean tile, float ux, float uy, float uw, float uh, float alpha) {

    }

    @Override
    public void setUiScale(float uiScale) {

    }

    @Override
    public FrameBufferObject getFBO(ResourceUrn urn, Vector2ic size) {
        return null;
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rectanglei drawRegion, Rectanglei cropRegion, Quaternionfc rotation, Vector3fc offset, float scale, float alpha) {

    }

    @Override
    public void drawMaterialAt(Material material, Rectanglei drawRegion) {

    }
}

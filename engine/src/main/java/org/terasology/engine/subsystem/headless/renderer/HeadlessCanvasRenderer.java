/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.headless.renderer;

import org.terasology.assets.ResourceUrn;
import org.terasology.math.Border;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.internal.CanvasRenderer;
import org.terasology.rendering.opengl.FrameBufferObject;

public class HeadlessCanvasRenderer implements CanvasRenderer {

    @Override
    public void preRender() {
        // Do nothing
    }

    @Override
    public void postRender() {
        // Do nothing
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rect2i drawRegion, Rect2i cropRegion, Quat4f rotation, Vector3f offset, float scale, float alpha) {
        // Do nothing
    }

    @Override
    public Vector2i getTargetSize() {
        return new Vector2i(0, 0);
    }

    @Override
    public void drawMaterialAt(Material material, Rect2i drawRegion) {
        // Do nothing
    }

    @Override
    public void drawLine(int sx, int sy, int ex, int ey, Color color) {
        // Do nothing
    }

    @Override
    public void crop(Rect2i cropRegion) {
        // Do nothing
    }

    @Override
    public FrameBufferObject getFBO(ResourceUrn uri, BaseVector2i region) {
        return null;
    }

    @Override
    public void drawTexture(TextureRegion texture, Color color, ScaleMode mode, Rect2i absoluteRegion, float ux, float uy, float uw, float uh, float alpha) {
        // Do nothing
    }

    @Override
    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rect2i absoluteRegion, Color color, Color shadowColor,
                         float alpha, boolean underlined) {
        // Do nothing
    }

    @Override
    public void drawTextureBordered(TextureRegion texture, Rect2i absoluteRegion, Border border, boolean tile, float ux, float uy, float uw, float uh, float alpha) {
        // Do nothing
    }
}

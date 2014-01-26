package org.terasology.rendering.nui.internal;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.nui.Color;

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
}

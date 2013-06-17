package org.terasology.rendering.renderer;

import org.terasology.math.AABB;

/**
 * @author Adeon
 */
public interface BlockOverlayRenderer {
    public void setAABB(AABB aabb);
    public void render(float lineThickness);
}

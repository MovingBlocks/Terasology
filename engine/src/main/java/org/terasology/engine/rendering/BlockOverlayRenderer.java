// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering;

import org.terasology.engine.math.AABB;

/**
 */
public interface BlockOverlayRenderer {
    void setAABB(AABB aabb);

    /**
     * Maintained for API compatibility
     */
    default void render(float lineThickness) {
        render();
    }

    void render();
}

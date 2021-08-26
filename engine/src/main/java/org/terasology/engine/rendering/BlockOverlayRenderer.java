// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering;

import org.terasology.joml.geom.AABBfc;

public interface BlockOverlayRenderer {
    void setAABB(AABBfc aabb);

    /**
     * Maintained for API compatibility
     */
    default void render(float lineThickness) {
        render();
    }

    void render();
}

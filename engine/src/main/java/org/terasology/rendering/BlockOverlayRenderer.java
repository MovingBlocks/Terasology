/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering;

import org.joml.AABBf;
import org.terasology.math.AABB;

/**
 * overlay rendering for a block based off the
 * containing region of an {@link AABB}
 */
public interface BlockOverlayRenderer {
    /**
     * set aabb that will be used for {@link BlockOverlayRenderer} to render region
     *
     * @param aabb contains region
     * @deprecated This method is scheduled for removal in an upcoming version.
     * Use the JOML implementation instead: {@link #setAABB(AABBf)}.
     **/
    @Deprecated
    void setAABB(AABB aabb);

    /**
     * set aabb that will be used for {@link BlockOverlayRenderer} to render region
     *
     * @param aabb contains region
     */
    void setAABB(AABBf aabb);

    /**
     * Maintained for API compatibility
     */
    default void render(float lineThickness) {
        render();
    }

    void render();
}

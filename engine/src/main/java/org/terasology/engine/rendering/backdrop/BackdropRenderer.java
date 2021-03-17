// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.backdrop;

import org.terasology.engine.rendering.cameras.Camera;

/**
 * Implementations of this interface are responsible for rendering the backdrop of terasology's world,
 * that is anything that is visually beyond the 3d scene renderered by the WorldRenderer.
 *
 * This is generally intended to be the sky, but the theatrical term "backdrop" is used to include
 * anything in the background. On the other hand, the term "background" itself was avoided as it is
 * used in the context of background/foreground processes and threads.
 */
@FunctionalInterface
public interface BackdropRenderer {

    /**
     * Renders the backdrop from the point of view provided by the camera, straight into the currently bound buffer.
     *
     * @param camera The camera providing the point of view to render the backdrop from.
     */
    void render(Camera camera);
}

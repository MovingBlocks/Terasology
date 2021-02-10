/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.backdrop;

import org.terasology.rendering.cameras.Camera;

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

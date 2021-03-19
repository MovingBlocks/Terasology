// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.backdrop;


import org.joml.Vector3f;

/**
 * Implementations of this interface provide read and/or write access to the backdrop,
 * the visual layer that is rendered beyond the 3d scene rendered by the WorldRenderer
 *
 * This is generally intended to be the sky, but the theatrical term "backdrop" is used to include
 * anything in the background. On the other hand, the term "background" itself was avoided as it is
 * used in the context of background/foreground processes and threads.
 */
public interface BackdropProvider {

    // Note: I intentionally leave this interface undocumented (which is how I found it
    // in the skysphere implementation) as I plan to radically change it. -- emanuele3d

    float getDaylight();

    float getTurbidity();

    float getColorExp();

    float getSunPositionAngle();

    Vector3f getSunDirection(boolean moonlightFlip);

}

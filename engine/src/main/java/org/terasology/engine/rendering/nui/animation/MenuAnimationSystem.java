// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.animation;

import org.terasology.joml.geom.Rectanglei;

/**
 * Controls animations to and from different screens
 */
public interface MenuAnimationSystem {

    /**
     * Trigger animation from previous screen to this one
     */
    void triggerFromPrev();

    /**
     * Trigger animation from this one back to the previous screen
     */
    void triggerToPrev();

    /**
     * Trigger animation from the next screen to this one
     */
    void triggerFromNext();

    /**
     * Trigger animation from this one to the next screen
     */
    void triggerToNext();

    /**
     * Use this method to show the next/previous screen.
     * @param listener the listener to trigger when the animation has ended
     */
    void onEnd(Runnable listener);

    /**
     * @param delta time difference in seconds
     */
    void update(float delta);

    /**
     * Transforms the provides area with respect to the current animations
     * @param rc the rect to transform
     * @return the transformed rectangle
     */
    Rectanglei animateRegion(Rectanglei rc);

    /**
     * Stops the current animation by skipping straight to the end
     */
    void skip();

    /**
     * Stops the current animation and plays the animation in reverse, if needed
     */
    void stop();
}

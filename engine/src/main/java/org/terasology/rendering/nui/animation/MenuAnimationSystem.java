/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.animation;

import org.joml.Rectanglei;

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

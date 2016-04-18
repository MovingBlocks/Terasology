/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.animation;

import java.util.ArrayList;
import java.util.List;

/*
 * Single animation that traverses frames.
 */
public class Animation {
    private final List<AnimationListener> listeners = new ArrayList<AnimationListener>();

    private RepeatMode repeatMode;

    private float elapsedTime;
    private int currentFrame;

    private TimeModifier timeModifier;

    private enum AnimState {
        STOPPED, PAUSED, RUNNING
    }

    private AnimState currentState = AnimState.STOPPED;

    private final float duration;

    private Interpolator<?> interpolator;

    /**
     * Constructs a new animation that runs once with linear speed.
     */
    public Animation(Interpolator<?> interpolator, float duration) {
        this(interpolator, duration, RepeatMode.RUN_ONCE, TimeModifiers.linear());
    }

    /**
     * Sets how the animation should repeat once it completes all the frames.
     *
     * @param repeatMode the repeat mode.
     */
    public Animation(Interpolator<?> interpolator, float duration, RepeatMode repeatMode, TimeModifier timeModifier) {
        this.interpolator = interpolator;
        this.duration = duration;
        this.repeatMode = repeatMode;
        this.timeModifier = timeModifier;
    }

    /**
     * Updates the animation if start() has been called and is not finished.
     *
     * @param delta elapsed time since last update, in seconds.
     */
    public void update(float delta) {
        if (currentState != AnimState.RUNNING) {
            return;
        }

        elapsedTime += delta;
        while (elapsedTime > duration) {
            elapsedTime -= duration;
            currentFrame++;

            if (repeatMode == RepeatMode.RUN_ONCE) {
                interpolator.apply(1f);
                end();
                return;
            }
        }

        float ipol = timeModifier.apply(elapsedTime / duration);
        interpolator.apply(ipol);
    }

    /**
     * Notifies that this animation has been set up and is ready for use.
     */
    public void start() {
        if (currentState == AnimState.STOPPED) {
            currentState = AnimState.RUNNING;
            for (AnimationListener li : this.listeners) {
                li.onStart();
            }
        }
    }

    /**
     * Notifies that this animation is finished or should end.
     */
    public void end() {
        if (currentState == AnimState.RUNNING) {
            currentState = AnimState.STOPPED;
            elapsedTime = 0;
            for (AnimationListener li : this.listeners) {
                li.onEnd();
            }
        }
    }

    /**
     * Stops an animation without signaling that it is finished and
     * maintains its current state.
     */
    public void pause() {
        if (currentState == AnimState.RUNNING) {
            currentState = AnimState.PAUSED;
        }
    }

    /**
     * Resumes a paused animation.
     */
    public void resume() {
        if (currentState == AnimState.PAUSED) {
            currentState = AnimState.RUNNING;
        }
    }

    /**
     * Adds a listener for animation events.
     *
     * @param li the listener for animation events
     */
    public void addListener(AnimationListener li) {
        this.listeners.add(li);
    }


    /**
     * Unsubscribes a listener from animation events.
     *
     * @param li the listener to stop receiving animation events for
     */
    public void removeListener(AnimationListener li) {
        this.listeners.remove(li);
    }
}

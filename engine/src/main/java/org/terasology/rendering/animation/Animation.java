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

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/*
 * Single animation that traverses frames.
 */
public class Animation {
    private final List<AnimationListener> listeners = new ArrayList<AnimationListener>();
    private final List<Frame> frames = new ArrayList<Frame>();
    private int repeatCount;
    private RepeatMode repeatMode;

    private float elapsedTime;
    private int currentFrame;
    private int currentRepeatCount;

    private enum AnimState {
        PRESTART, PAUSED, RUNNING, FINISHED
    }
    private AnimState currentState;

    /**
     * Constructs a new animation with default properties.
     */
    public Animation() {
        currentState = AnimState.PRESTART;
        currentRepeatCount = 0;
        repeatCount = 0;
        elapsedTime = 0;
        currentFrame = 0;
        repeatMode = RepeatMode.REPEAT_INFINITE;
    }

    /**
     * Adds a frame to the end.
     *
     * @param frame the frame to add.
     */
    public void addFrame(Frame frame) {
        this.frames.add(frame);
    }

    /**
     * Sets the number of times this animation must repeat.
     *
     * @param repeat number of times this animation must repeat, must be positive
     *
     * @throws IllegalArgumentException if repeat is not positive
     */
    public void setRepeatCount(int repeat) {
        if (!(repeat > 0)) {
            throw new IllegalArgumentException("repeat must be positive");
        }
        this.repeatCount = repeat;
    }

    /**
     * Sets how the animation should repeat once it completes all the frames.
     *
     * @param repeat the repeat mode.
     */
    public void setRepeatMode(RepeatMode repeat) {
        this.repeatMode = repeat;
    }

    /**
     * Updates the animation if start() has been called and is not finished.
     *
     * @param delta elapsed time since last update, in seconds.
     */
    public void update(float delta) {
        switch (this.currentState) {
        case RUNNING: {
            frames.get(currentFrame).update(delta);
            if (frames.get(currentFrame).isFinished()) {
                currentFrame++;
                if (currentFrame >= frames.size()) {
                    currentRepeatCount++;
                    switch (repeatMode) {
                    case RUN_ONCE:
                        end();
                        break;
                    case REPEAT: case INVERSE:
                        if (currentRepeatCount >= repeatCount) {
                            end();
                            break;
                        }
                        if (repeatMode.equals(RepeatMode.INVERSE)) {
                            flipFrames();
                        }
                        currentFrame = 0;
                        elapsedTime = 0;
                        break;
                    case REPEAT_INFINITE: case INVERSE_INFINITE:
                        if (repeatMode.equals(RepeatMode.INVERSE) || repeatMode.equals(RepeatMode.INVERSE_INFINITE)) {
                            flipFrames();
                        }
                        currentFrame = 0;
                        elapsedTime = 0;
                        break;
                    }
                }
            }
            break;
        }
        default: break;
        }
    }

    /**
     * Reverses the animation completely.
     */
    public void flipFrames() {
        Collections.reverse(frames);
        for (int i = 0; i < frames.size(); i++) {
            frames.get(i).reverse();
        }
    }

    /**
     * Notifies that this animation has been set up and is ready for use.
     */
    public void start() {
        if (this.currentState.equals(AnimState.PRESTART)) {
            elapsedTime = 0;
            currentRepeatCount = 0;
            this.currentState = AnimState.RUNNING;
            for (AnimationListener li : this.listeners) {
                li.onStart();
            }
        }
    }

    /**
     * Notifies that this animation is finished or should end.
     */
    public void end() {
        if (this.currentState.equals(AnimState.RUNNING)) {
            this.currentState = AnimState.FINISHED;
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
        if (this.currentState.equals(AnimState.RUNNING)) {
            this.currentState = AnimState.PAUSED;
        }
    }

    /**
     * Resumes a paused animation.
     */
    public void resume() {
        if (this.currentState.equals(AnimState.PAUSED)) {
            this.currentState = AnimState.RUNNING;
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

    /**
     * Returns if the animation has completed or has ended.
     *
     * @return if the animation has completed or has ended
     */
    public boolean isFinished() {
        return currentState.equals(AnimState.FINISHED);
    }
}

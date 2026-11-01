// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/*
 * Single animation that traverses frames.
 */
public final class Animation {

    private enum AnimState {
        STOPPED, PAUSED, RUNNING
    }

    private enum RepeatMode {
        RUN_ONCE,
        REPEAT_INFINITE
    }

    private enum Direction {
        FORWARD,
        REVERSE;
    }

    private final List<AnimationListener> listeners = new ArrayList<AnimationListener>();

    private final RepeatMode repeatMode;
    private final float duration;
    private final Animator animator;
    private final TimeModifier timeModifier;

    private Direction direction = Direction.FORWARD;
    private AnimState currentState = AnimState.STOPPED;
    private float elapsedTime;

    /**
     * @param animator the animator that is updated over time
     * @param duration the duration in seconds (must be positive)
     * @param repeatMode the repeat mode
     * @param timeModifier the time modifier to apply
     */
    private Animation(Animator animator, float duration, RepeatMode repeatMode, TimeModifier timeModifier) {
        Preconditions.checkArgument(animator != null);
        Preconditions.checkArgument(repeatMode != null);
        Preconditions.checkArgument(timeModifier != null);
        Preconditions.checkArgument(duration > 0);

        this.animator = animator;
        this.duration = duration;
        this.repeatMode = repeatMode;
        this.timeModifier = timeModifier;
    }

    /**
     * Constructs a new animation that runs once with linear speed.
     * @param animator the animator that is updated over time
     * @param duration the duration in seconds
     * @param timeModifier the time modifier to apply
     * @return the animation
     */
    public static Animation once(Animator animator, float duration, TimeModifier timeModifier) {
        return new Animation(animator, duration, RepeatMode.RUN_ONCE, timeModifier);
    }

    /**
     * Creates an animation that loops infinitely
     * @param animator the animator that is updated over time
     * @param duration the duration in seconds (must be positive)
     * @param timeModifier the time modifier to apply
     * @return the animation
     */
    public static Animation infinite(Animator animator, float duration, TimeModifier timeModifier) {
        return new Animation(animator, duration, RepeatMode.REPEAT_INFINITE, timeModifier);
    }

    /**
     * Plays the animation forwards. Can be set at any time.
     * @return this
     */
    public Animation setForwardMode() {
        return setDirection(Direction.FORWARD);
    }

    /**
     * Plays the animation reverse. Can be set at any time.
     * @return this
     */
    public Animation setReverseMode() {
        return setDirection(Direction.REVERSE);
    }

    /**
     * @return true if in reverse mode, false otherwise
     */
    public boolean isReverse() {
        return direction == Direction.REVERSE;
    }

    private Animation setDirection(Direction newDir) {
        if (direction != newDir) {
            direction = newDir;
            elapsedTime = duration - elapsedTime;
        }
        return this;
    }

    /**
     * Updates the animation if {@link #start} has been called and is not finished.
     *
     * @param delta elapsed time since last update, in seconds.
     */
    public void update(float delta) {
        if (currentState != AnimState.RUNNING) {
            return;
        }

        elapsedTime += delta;
        if (elapsedTime >= duration) {
            if (repeatMode == RepeatMode.RUN_ONCE) {
                elapsedTime = duration;
                stop();
                return;
            } else {
                elapsedTime %= duration;
            }
        }

        updateAnimator();
    }

    private void updateAnimator() {
        float time = direction == Direction.FORWARD ? elapsedTime : duration - elapsedTime;
        float ipol = timeModifier.apply(time / duration);
        animator.apply(ipol);
    }

    /**
     * Notifies that this animation has been set up and is ready for use.
     * @return this
     */
    public Animation start() {
        if (currentState == AnimState.STOPPED) {
            currentState = AnimState.RUNNING;
            elapsedTime = 0;
            for (AnimationListener li : this.listeners) {
                li.onStart();
            }
            updateAnimator();
        }
        return this;
    }

    /**
     * Notifies that this animation is finished or should end.
     * @return this
     */
    public Animation stop() {
        if (currentState == AnimState.RUNNING) {
            currentState = AnimState.STOPPED;
            updateAnimator();
            for (AnimationListener li : this.listeners) {
                li.onEnd();
            }
        }
        return this;
    }

    /**
     * Stops an animation without signaling that it is finished and
     * maintains its current state.
     * @return this
     */
    public Animation pause() {
        if (currentState == AnimState.RUNNING) {
            currentState = AnimState.PAUSED;
        }
        return this;
    }

    /**
     * Resumes a paused animation.
     * @return this
     */
    public Animation resume() {
        if (currentState == AnimState.PAUSED) {
            currentState = AnimState.RUNNING;
        }
        return this;
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
     * Unsubscribes all listener from animation events.
     */
    public void removeAllListeners() {
        this.listeners.clear();
    }

    /**
     * @return true if stopped, false otherwise
     */
    public boolean isStopped() {
        return currentState == AnimState.STOPPED;
    }

    /**
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return currentState == AnimState.RUNNING;
    }

    /**
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return currentState == AnimState.PAUSED;
    }

    /**
     * @return the duration of the animation in seconds
     */
    public float getDuration() {
        return duration;
    }
}

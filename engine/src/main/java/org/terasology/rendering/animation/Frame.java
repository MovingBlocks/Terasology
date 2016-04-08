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
 * Updates animation components simultaneously.
 */
public class Frame {
    private final List<Object> fromComponents = new ArrayList<Object>();
    private final List<Object> toComponents = new ArrayList<Object>();
    private final List<FrameComponentInterface> compInterfaces = new ArrayList<FrameComponentInterface>();
    private final List<Interpolator> compInterpolators = new ArrayList<Interpolator>();

    private int repeatCount;
    private RepeatMode repeatMode;
    private float startDelay;
    private float duration;

    private float elapsedTime;
    private int currentRepeatCount;

    /**
     * Constructs a new linear-interpolated, 1 sec animation frame.
     */
    public Frame() {
        currentRepeatCount = 0;
        repeatCount = 0;
        startDelay = 0;
        duration = 1;
        elapsedTime = 0;
        repeatMode = RepeatMode.RUN_ONCE;
    }

    /**
     * Progresses the animation.
     *
     * @param delta time elapsed since last update, in seconds
     */
    public void update(float delta) {
        elapsedTime += delta;
        float tval = (elapsedTime - startDelay) / duration;
        if (elapsedTime > startDelay + duration) {
            currentRepeatCount++;
            tval = 1;
            elapsedTime = 0;
        }
        if (elapsedTime >= startDelay) {
            for (int i = 0; i < compInterfaces.size(); i++) {
                float val = compInterpolators.get(i)
                    .getInterpolation(tval);
                Object oval = compInterfaces.get(i)
                    .computeInterpolation(val,
                                          fromComponents.get(i),
                                          toComponents.get(i));
                compInterfaces.get(i).setValue(oval);
            }
        }
    }

    /**
     * Reverses the frame such that it will progress backwards.
     */
    public void reverse() {
        Collections.reverse(fromComponents);
        Collections.reverse(toComponents);
        Collections.reverse(compInterfaces);
        Collections.reverse(compInterpolators);
    }

    /**
     * Adds a new component to be animated for frames that do not start
     * from a previous frame. Could be a position, color, sound,
     * texture, etc.
     *
     * @param from the component to start from
     * @param to the component to end at
     * @param compInterface the component interpolation interface
     */
    public void addComponent(Object from, Object to,
                             FrameComponentInterface compInterface, Interpolator interpolator) {
        if (interpolator == null) {
            throw new NullPointerException("interpolator must not be null");
        }
        fromComponents.add(from);
        toComponents.add(to);
        compInterfaces.add(compInterface);
        compInterpolators.add(interpolator);
    }

    /**
     * Adds a new component to be animated for frames that do not start
     * from a previous frame. Could be a position, color, sound,
     * texture, etc.
     *
     * @param from the component to start from
     * @param to the component to end at
     * @param compInterface the component interpolation interface
     */
    public void addComponent(Object from, Object to,
                             FrameComponentInterface compInterface) {
        addComponent(from, to, compInterface, new BaseInterpolator());
    }

    /**
     * Sets the repeat count and type. If count greater than 0 the
     * animation repeats count types, else if count == 0 then it
     * repeats forever, else if count less than -1 then it reverse
     * repeates count times, and if count is -1 then it reverse
     * repeats forever.
     *
     * @param count the repeat count and type
     */
    public void setRepeatCount(int count) {
        if (!(count > 0)) {
            throw new IllegalArgumentException("repeat must be positive");
        }
        this.repeatCount = count;
    }

    /**
     * Sets how the animation should behave when it reaches the end of the frame.
     *
     * @param repeatMode the repeat mode.
     */
    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    /**
     * Sets the start delay for the frame.
     *
     * @param the start delay for the frame
     */
    public void setStartDelay(float delay) {
        if (delay <= 0) {
            throw new IllegalArgumentException("delay must be greater than 0");
        }
        startDelay = delay;
    }

    /**
     * Sets the duration this frame should take to complete.
     *
     * @param duration the duration this frame should take to complete
     */
    public final void setDuration(float duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException(
                "duration must be greater than 0");
        }
        this.duration = duration;
    }

    /**
     * Returns if the animation has completed.
     *
     * @return if the animation has completed
     */
    public final boolean isFinished() {
        switch (repeatMode) {
        case RUN_ONCE:
            return currentRepeatCount >= 1;
        case REPEAT: case INVERSE:
            return currentRepeatCount >= repeatCount;
        case REPEAT_INFINITE: case INVERSE_INFINITE:
            return false;
        default:
            return true;
        }
    }

    /**
     * Abstracts calculating interpolated data.
     */
    public interface FrameComponentInterface {
        /**
         * Interpolates between two values on a linear scale.
         *
         * @param v the intermediate interpolation value
         * @param theFrom the left hand value to interpolate between
         * @param toFrom the right hand value to interpolate between
         *
         * @return the computed interpolated value
         */
        Object computeInterpolation(float v, Object theFrom, Object theTo);

        /**
         * Recieves a computed interpolation value.
         *
         * @param value the interpolated value
         */
        void setValue(Object value);
    }
}

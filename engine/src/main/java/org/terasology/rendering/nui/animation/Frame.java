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
package org.terasology.rendering.nui.animation;

import java.util.List;
import java.util.ArrayList;

/*
 */
public class Frame {
    private List<Object> fromComponents;
    private List<Object> toComponents;
    private List<FrameComponentInterface> compInterfaces;
    private Interpolator interpolator;

    private int repeat;
    private float startDelay;
    private float duration;

    /** If the frame starts from the last, override fromComponents */
    private Frame lastFrame;

    /**
     * Constructs a new linear-interpolated, 1 sec animation frame.
     *
     * @param startFromLastFrame if non-null, will allow using the to
     * components from another frame as the from components for this frame,
     * otherwise components must be supplied for from and to equally.
     */
    public Frame(Frame startFromLastFrame) {
        lastFrame = startFromLastFrame;
        if (lastFrame == null) {
            fromComponents = new ArrayList<Object>();
        }
        toComponents = new ArrayList<Object>();
        compInterfaces = new ArrayList<FrameComponentInterface>();
        interpolator = new BaseInterpolator();
        repeat = 0;
        startDelay = 0;
        duration = 1000;
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
        if (lastFrame != null) {
            throw new IllegalStateException("Cannot add from components when using last frame");
        }
        fromComponents.add(from);
        toComponents.add(to);
        compInterfaces.add(compInterface);
    }

    /**
     * Adds a new component to be animated for frames that <b>DO</b>
     * start from a previous frame. Could be a position, color, sound,
     * texture, etc.
     *
     * @param to the component to end at
     * @param compInterface the component interpolation interface
     */
    public void addComponent(Object to, FrameComponentInterface compInterface) {
        if (lastFrame == null) {
            throw new IllegalStateException("Must have a start frame to add from components");
        }
        toComponents.add(to);
        compInterfaces.add(compInterface);
    }

    /**
     * Sets the interpolator this frame uses.
     *
     * @param interpolator the interpolator this frame uses.
     */
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            throw new NullPointerException("interpolator must not be null");
        }
        this.interpolator = interpolator;
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
    public void setRepeat(int count) {
        repeat = count;
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
     * Returns the duration this frame will take to complete.
     *
     * @return the duration this frame will take to complete
     */
    public final float getDuration() {
        return duration;
    }

    public interface FrameComponentInterface {
        /**
         * Interpolates between two values on a linear scale.
         *
         * @param v the intermediate interpolation value
         * @param theFrom the left hand value to interpolate between
         * @param toFrom the right hand value to interpolate between
         */
        void setInterpolation(float v, Object theFrom, Object theTo);
    }
}

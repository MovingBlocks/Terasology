/*
 * Copyright 2014 MovingBlocks
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
public abstract class AbstractAnimation {
    public static enum RepeatType {
        REPEAT, REVERSE
    }

    public static interface InterpolatorInterface {
        /**
         * Returns where an interpolated value should be based on
         * where the position an animation is in.
         *
         * @param v position of the animation between the start and end [0:1]
         * Or between [setStart:setEnd] if they have been called.
         *
         * @return where the interpolated value should be
         */
        float getInterpolation(float v);

        void setStart(float v);
        float getStart();

        void setEnd(float v);
        float getEnd();
    }

    public static interface FrameComponentInterface<T> {
        void setInterpolation(float v, T theFrom, T theTo);
    }

    public static class AbstractFrame {
        private List<Object> myFromComponents;
        private List<Object> myToComponents;
        private FrameComponentInterface myInterface;
        private InterpolatorInterface myInterpolation;

        private RepeatType myRepeatType;
        private int myRepeatCount;
        private float myStartDelay, myDuration;
        private boolean myStartUseLastFrame;
    }

    public static interface AnimationListener {
        void onStart();
        void onFrameStart();
        void onStep(float v);
        void onFrameEnd(int repeatCount);
        void onEnd(int repeatCount);
    }

    public static class AnimationAdapter implements AnimationListener {
        public void onStart() {}
        public void onFrameStart() {}
        public void onStep(float v) {}
        public void onFrameEnd(int repeatCount) {}
        public void onEnd(int repeatCount) {}
    }

    private List<AnimationListener> myListeners;
    private List<AbstractFrame> myFrames;

    /** If the current frame starts from the last */
    private List<AbstractFrame> myLastFrame;

    private float myCurrentDuration;
    private int myCurrentRepeatCount;

    private RepeatType myRepeatType;
    private int myRepeatCount;

    private static enum AnimState {
        STOPPED, RUNNING
    }
    private AnimState myState;

    public AbstractAnimation() {
        myListeners = new ArrayList<AnimationListener>();
        myFrames = new ArrayList<AbstractFrame>();
        myLastFrame = null;
        myState = AnimState.STOPPED;
    }

    public void addFrame(AbstractFrame theFrame) {
        myFrames.add(theFrame);
    }

    public void start() {
        myState = AnimState.RUNNING;
        for (AnimationListener li : myListeners) {
            li.onStart();
        }
    }

    public void update(float dt) {
        switch (myState) {
        case STOPPED:
            return;
        case RUNNING: {
            if (myFrames.size() == 0) {
                onEnd();
                return;
            }
            myCurrentDuration += dt;
            if (myCurrentDuration >= myFrames.get(0).getDuration()) {
                myLastFrame = myFrames.get(0);
                myFrames.remove(0);
                return;
            }
            break;
        }
        }
    }

    private void onEnd() {
        myState = AnimState.STOPPED;
        for (AnimationListener li : myListeners) {
            li.onEnd(myRepeatCount);
        }
    }

    public void addListener(AnimationListener li) {
        myListeners.add(li);
    }

    public void removeListener(AnimationListener li) {
        myListeners.remove(li);
    }
}

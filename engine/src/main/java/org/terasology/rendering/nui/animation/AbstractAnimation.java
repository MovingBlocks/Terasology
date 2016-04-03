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

import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

import java.util.List;
import java.util.ArrayList;

/*
 */
public abstract class AbstractAnimation implements UpdateSubscriberSystem {
    private List<AnimationListener> listeners;
    private List<Frame> frames;
    private int repeat;

    private float elapsedTime;
    private int currentRepeatCount;

    private enum AnimState {
        STOPPED, PAUSED, RUNNING
    }
    private AnimState currentState;

    public AbstractAnimation() {
        listeners = new ArrayList<AnimationListener>();
        frames = new ArrayList<Frame>();
        currentState = AnimState.STOPPED;
        repeat = 0;
    }

    public void addFrame(Frame frame) {
        this.frames.add(frame);
    }

    public void start() {
        elapsedTime = 0;
        currentRepeatCount = 0;
        this.currentState = AnimState.RUNNING;
        for (AnimationListener li : this.listeners) {
            li.onStart();
        }
    }

    public void update(float delta) {
        switch (this.currentState) {
        case PAUSED: case STOPPED:
            return;
        case RUNNING: {
            if (this.frames.size() == 0) {
                onEnd();
                return;
            }
            elapsedTime += delta;
            if (elapsedTime >= frames.get(0).getDuration()) {
                frames.remove(0);
                return;
            }
            break;
        }
        }
    }

    private void onEnd() {
        this.currentState = AnimState.STOPPED;
        for (AnimationListener li : this.listeners) {
            li.onEnd(this.currentRepeatCount);
        }
    }

    public void addListener(AnimationListener li) {
        this.listeners.add(li);
    }

    public void removeListener(AnimationListener li) {
        this.listeners.remove(li);
    }

    public interface AnimationListener {
        void onStart();
        void onFrameStart();
        void onStep(float v);
        void onFrameEnd(int repeatCount);
        void onEnd(int repeatCount);
    }

    public static class AnimationAdapter implements AnimationListener {
        public void onStart() { }
        public void onFrameStart() { }
        public void onStep(float v) { }
        public void onFrameEnd(int repeatCount) { }
        public void onEnd(int repeatCount) { }
    }
}

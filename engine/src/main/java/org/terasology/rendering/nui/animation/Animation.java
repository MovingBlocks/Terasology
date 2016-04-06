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

import org.terasology.assets.AssetData;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/*
 */
public class Animation implements Component, AssetData {
    private List<AnimationListener> listeners;
    private List<Frame> frames;
    private int repeat;

    private float elapsedTime;
    private int currentFrame;
    private int currentRepeatCount;

    private enum AnimState {
        PRESTART, PAUSED, RUNNING, FINISHED
    }
    private AnimState currentState;

    public Animation() {
        listeners = new ArrayList<AnimationListener>();
        frames = new ArrayList<Frame>();
        currentState = AnimState.PRESTART;
        repeat = 0;
        elapsedTime = 0;
        currentFrame = 0;
    }

    public void addFrame(Frame frame) {
        this.frames.add(frame);
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void update(float delta) {
        switch (this.currentState) {
        case RUNNING: {
            frames.get(currentFrame).update(delta);
            if (frames.get(currentFrame).isFinished()) {
                currentFrame++;
                if (currentFrame >= frames.size()) {
                    currentRepeatCount++;
                    if (currentRepeatCount >= repeat ||
                        currentRepeatCount >= -(repeat + 1)) {
                        end();
                    } else { // if repeating forever or still able to repeat
                        currentFrame = 0;
                        elapsedTime = 0;//startDelay;
                    }
                }
            }
            break;
        }
        default: break;
        }
    }

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

    public void end() {
        if (this.currentState.equals(AnimState.RUNNING)) {
            this.currentState = AnimState.FINISHED;
            for (AnimationListener li : this.listeners) {
                li.onEnd(this.currentRepeatCount);
            }
        }
    }

    public void pause() {
        if (this.currentState.equals(AnimState.RUNNING)) {
            this.currentState = AnimState.PAUSED;
        }
    }

    public void resume() {
        if (this.currentState.equals(AnimState.PAUSED)) {
            this.currentState = AnimState.RUNNING;
        }
    }

    public void addListener(AnimationListener li) {
        this.listeners.add(li);
    }

    public void removeListener(AnimationListener li) {
        this.listeners.remove(li);
    }

    public boolean isFinished() {
        return currentState.equals(AnimState.FINISHED);
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

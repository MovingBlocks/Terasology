/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.rendering.gui.animation;

import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.AnimationListener;

import java.util.ArrayList;

/**
 * TODO notification for repeat event
 */
public abstract class Animation {

    //events
    private enum EAnimationEvents {
        START, STOP, REPEAT
    }

    ;
    private final ArrayList<AnimationListener> animationListeners = new ArrayList<AnimationListener>();

    private boolean started = false;
    private boolean repeat = false;
    protected UIDisplayElement target;


    public void start() {
        started = true;
        notifyAnimationListeners(EAnimationEvents.START);
    }

    ;

    public void stop() {
        started = false;
        notifyAnimationListeners(EAnimationEvents.STOP);
    }

    ;

    public boolean isStarted() {
        return started;
    }

    public void setTarget(UIDisplayElement target) {
        this.target = target;
    }

    ;

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public abstract void renderBegin();

    public abstract void renderEnd();

    public abstract void update();

    public void addAnimationListener(AnimationListener listener) {
        animationListeners.add(listener);
    }

    public void removeAnimationListener(AnimationListener listener) {
        animationListeners.remove(listener);
    }

    private void notifyAnimationListeners(EAnimationEvents event) {
        if (event == EAnimationEvents.START) {
            for (AnimationListener listener : animationListeners) {
                listener.start(target);
            }
        } else if (event == EAnimationEvents.STOP) {
            for (AnimationListener listener : animationListeners) {
                listener.stop(target);
            }
        } else if (event == EAnimationEvents.REPEAT) {
            for (AnimationListener listener : animationListeners) {
                listener.repeat(target);
            }
        }
    }

}

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

package org.terasology.rendering.nui;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.animation.Animation;
import org.terasology.rendering.animation.AnimationListener;
import org.terasology.rendering.animation.TimeModifiers;

/**
 * Controls animations to and from different screens
 */
public class MenuAnimationSystem {

    private Animation flyIn;
    private Animation flyOut;

    private float scale;

    /**
     * Creates default animations
     */
    public MenuAnimationSystem() {
        flyIn = Animation.once(v -> scale = v, 1.2f, TimeModifiers.inverse().andThen(TimeModifiers.square()));
        flyOut = Animation.once(v -> scale = -v, 1.2f, TimeModifiers.square());
//        flyIn = Animation.once(v -> scale = -v, 1.2f, TimeModifiers.inverse().andThen(TimeModifiers.square()));
//        flyOut = Animation.once(v -> scale = v, 1.2f, TimeModifiers.square());
    }

    /**
     * Trigger animation from previous screen to this one
     */
    public void triggerFromPrev() {
        flyIn.setForwardMode();
        flyIn.start();
    }

    /**
     * Trigger animation from this one back to the previous screen
     */
    public void triggerToPrev() {
        flyIn.setReverseMode();
        flyIn.start();
    }

    /**
     * Trigger animation from the next screen to this one
     */
    public void triggerFromNext() {
        flyOut.setReverseMode();
        flyOut.start();
    }

    /**
     * Trigger animation from this one to the next screen
     */
    public void triggerToNext() {
        flyOut.setForwardMode();
        flyOut.start();
    }

    /**
     * @param listener the listener to trigger when the animation has ended
     */
    public void onEnd(Runnable listener) {
        flyOut.removeAllListeners();
        flyOut.addListener(new AnimationListener() {
            @Override
            public void onEnd() {
                if (!flyOut.isReverse()) {
                    listener.run();
                }
            }
        });

        flyIn.removeAllListeners();
        flyIn.addListener(new AnimationListener() {
            @Override
            public void onEnd() {
                if (flyIn.isReverse()) {
                    listener.run();
                }
            }
        });
    }

    /**
     * @param delta time difference in seconds
     */
    public void update(float delta) {
        flyIn.update(delta);
        flyOut.update(delta);
    }

    public Rect2i animateRegion(Rect2i rc) {
        if (scale == 0.0) {
            // this should cover most of the cases
            return rc;
        }

        int left = (int) (scale * rc.width());
        return Rect2i.createFromMinAndSize(left, 0, rc.width(), rc.height());
    }

}

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

package org.terasology.rendering.nui.animation;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.animation.Animation;
import org.terasology.rendering.animation.AnimationListener;
import org.terasology.rendering.animation.TimeModifiers;

/**
 * Controls animations to and from different screens
 */
public class SwipeMenuAnimationSystem implements MenuAnimationSystem {

    public enum Direction {
        LEFT_TO_RIGHT(1, 0),
        RIGHT_TO_LEFT(-1, 0),
        TOP_TO_BOTTOM(0, -1),
        BOTTOM_TO_TOP(0, 1);

        private final float horzScale;
        private final float vertScale;

        Direction(float horzScale, float vertScale) {
            this.horzScale = horzScale;
            this.vertScale = vertScale;
        }

        public float getHorzScale() {
            return horzScale;
        }

        public float getVertScale() {
            return vertScale;
        }
    }

    private final Direction direction;

    private final Animation flyIn;
    private final Animation flyOut;

    private float scale;

    /**
     * Creates default animations
     * @param duration the duration of the animation in seconds
     */
    public SwipeMenuAnimationSystem(float duration) {
        this(duration, Direction.LEFT_TO_RIGHT);
    }

    /**
     * Creates default animations
     * @param duration the duration of the animation in seconds
     * @param direction the swipe direction
     */
    public SwipeMenuAnimationSystem(float duration, Direction direction) {
        // down from 1 (fast) to 0 (slow)
        flyIn = Animation.once(v -> scale = v, duration, TimeModifiers.inverse().andThen(TimeModifiers.square()));

        // down from 0 (slow) to -1 (fast)
        flyOut = Animation.once(v -> scale = -v, duration, TimeModifiers.square());

        this.direction = direction;
    }

    /**
     * Trigger animation from previous screen to this one
     */
    @Override
    public void triggerFromPrev() {
        if (flyOut.isStopped()) {
            flyIn.setForwardMode();
            flyIn.start();
        }
    }

    /**
     * Trigger animation from this one back to the previous screen
     */
    @Override
    public void triggerToPrev() {
        if (flyOut.isStopped()) {
            flyIn.setReverseMode();
            flyIn.start();
        }
    }

    /**
     * Trigger animation from the next screen to this one
     */
    @Override
    public void triggerFromNext() {
        if (flyIn.isStopped()) {
            flyOut.setReverseMode();
            flyOut.start();
        }
    }

    /**
     * Trigger animation from this one to the next screen
     */
    @Override
    public void triggerToNext() {
        if (flyIn.isStopped()) {
            flyOut.setForwardMode();
            flyOut.start();
        }
    }

    @Override
    public void skip() {
        // set the animation to the end point and trigger onEnd()
        if (flyIn.isRunning()) {
            flyIn.update(flyIn.getDuration());
        }
        if (flyOut.isRunning()) {
            flyOut.update(flyOut.getDuration());
        }
    }

    @Override
    public void stop() {
        if (flyOut.isRunning()) {
            flyOut.setReverseMode();
        }
        if (flyIn.isRunning()) {
            flyIn.setForwardMode();
        }
    }

    /**
     * @param listener the listener to trigger when the animation has ended
     */
    @Override
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
    @Override
    public void update(float delta) {
        float animDelta = delta;

        if (animDelta > 0.1f) {
            // avoid skipping over fast animations on heavy load
            animDelta = 0.1f;
        }
        if(animDelta == 0.0f) {
            // in case time is paused
            animDelta = 0.035f;
        }

        flyIn.update(animDelta);
        flyOut.update(animDelta);
    }   

    @Override
    public Rect2i animateRegion(Rect2i rc) {
        if (scale == 0.0) {
            // this should cover most of the cases
            return rc;
        }

        int left = (int) (direction.getHorzScale() * scale * rc.width());
        int top = (int) (direction.getVertScale() * scale * rc.height());
        return Rect2i.createFromMinAndSize(left, top, rc.width(), rc.height());
    }

}

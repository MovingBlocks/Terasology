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
 * TODO Type description
 */
public class MenuAnimationSystem {

    private Animation flyIn;
    private Animation flyOut;

    private float scale;

    /**
     *
     */
    public MenuAnimationSystem() {

        flyIn = Animation.once(v -> scale = -v, 1.2f, TimeModifiers.inverse().andThen(TimeModifiers.square()));
        flyOut = Animation.once(v -> scale = v, 1.2f, TimeModifiers.square());
    }

    public void triggerStart() {
        flyIn.start();
    }

    /**
     *
     */
    public void triggerEnd() {
        flyOut.start();
    }

    /**
     * @param listener
     */
    public void onEnd(Runnable listener) {
        flyOut.removeAllListeners();
        flyOut.addListener(new AnimationListener() {
            @Override
            public void onEnd() {
                listener.run();
            }
        });
    }

    /**
     * @param delta
     */
    public void update(float delta) {
        flyIn.update(delta);
        flyOut.update(delta);
    }

    public Rect2i getRenderRegion(Rect2i rc) {
        int left = (int) (scale * rc.width());
        return Rect2i.createFromMinAndSize(left, 0, rc.width(), rc.height());
    }

}

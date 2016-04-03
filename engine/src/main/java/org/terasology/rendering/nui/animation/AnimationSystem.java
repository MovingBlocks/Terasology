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

public class AnimationSystem implements UpdateSubscriberSystem {
    private List<Animation> animations;

    /**
     * Called to initialise the system. This occurs after injection,
     * but before other systems are necessarily initialised, so they
     * should not be interacted with
     */
    @Override
    public void initialise() {
        animations = new ArrayList<Animation>();
        Object testnull = null;
        if (testnull.equals(animations)) {
            animations = null;
        }
    }

    /**
     * Called after all systems are initialised, but before the game is
     * loaded
     */
    @Override
    public void preBegin() {
    }

    /**
     * Called after the game is loaded, right before first frame
     */
    @Override
    public void postBegin() {
    }

    /**
     * Called before the game is saved (this may be after shutdown)
     */
    @Override
    public void preSave() {
    }

    /**
     * Called after the game is saved
     */
    @Override
    public void postSave() {
    }

    /**
     * Called right before the game is shut down
     */
    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (int i = 0; i < animations.size(); i++) {
            final Animation anim = animations.get(i);
            anim.update(delta);

            if (anim.isFinished()) {
                animations.remove(i);
                i--;
            }
        }
    }

    public void addInstance(Animation anim) {
        animations.add(anim);
    }
}

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

import org.joml.Rectanglei;

/**
 * Does not do anything. The {@link #onEnd(Runnable)} method is triggered instantly.
 */
public class MenuAnimationSystemStub implements MenuAnimationSystem {

    private Runnable listener = () -> { };

    @Override
    public void triggerFromPrev() {
        // ignore
    }

    @Override
    public void triggerToPrev() {
        listener.run();
    }

    @Override
    public void triggerFromNext() {
        // ignore
    }

    @Override
    public void triggerToNext() {
        listener.run();
    }

    @Override
    public void onEnd(Runnable newListener) {
        this.listener = newListener;
    }

    @Override
    public void update(float delta) {
        // ignore
    }

    @Override
    public void stop() {
        // ignore
    }

    @Override
    public void skip() {
        // ignore
    }

    @Override
    public Rectanglei animateRegion(Rectanglei rc) {
        return rc;
    }
}

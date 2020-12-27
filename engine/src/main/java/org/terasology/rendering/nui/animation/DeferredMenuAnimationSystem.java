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

import java.util.function.Supplier;

import org.joml.Rectanglei;
import org.terasology.math.geom.Rect2i;

/**
 * Forwards all calls to a {@link MenuAnimationSystem} from a provider.
 */
public class DeferredMenuAnimationSystem implements MenuAnimationSystem {

    private final Supplier<MenuAnimationSystem> provider;

    public DeferredMenuAnimationSystem(Supplier<MenuAnimationSystem> provider) {
        this.provider = provider;
    }

    @Override
    public void triggerFromPrev() {
        getSystem().triggerFromPrev();
    }

    @Override
    public void triggerToPrev() {
        getSystem().triggerToPrev();
    }

    @Override
    public void triggerFromNext() {
        getSystem().triggerFromNext();
    }

    @Override
    public void triggerToNext() {
        getSystem().triggerToNext();
    }

    @Override
    public void onEnd(Runnable newListener) {
        getSystem().onEnd(newListener);
    }

    @Override
    public void update(float delta) {
        getSystem().update(delta);
    }

    @Override
    public void skip() {
        getSystem().skip();
    }

    @Override
    public void stop() {
        getSystem().stop();
    }

    @Override
    public Rectanglei animateRegion(Rectanglei rc) {
        return getSystem().animateRegion(rc);
    }

    private MenuAnimationSystem getSystem() {
        return provider.get();
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.animation;

import org.terasology.joml.geom.Rectanglei;

import java.util.function.Supplier;

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

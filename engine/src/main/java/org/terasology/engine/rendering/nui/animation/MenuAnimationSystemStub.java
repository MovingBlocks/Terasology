// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.animation;

import org.terasology.math.geom.Rect2i;

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
    public Rect2i animateRegion(Rect2i rc) {
        return rc;
    }
}
